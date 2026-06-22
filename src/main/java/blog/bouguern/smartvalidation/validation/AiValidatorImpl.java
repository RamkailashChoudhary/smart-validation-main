package blog.bouguern.smartvalidation.validation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AiValidatorImpl implements AiValidator {

	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	private final long timeoutMs;
	private final boolean fallbackOnAiFailure;

	public AiValidatorImpl(ChatClient chatClient, ObjectMapper objectMapper,
			@Value("${app.validation.timeout-ms:5000}") long timeoutMs,
			@Value("${app.validation.fallback-on-ai-failure:true}") boolean fallbackOnAiFailure) {
		this.chatClient = chatClient;
		this.objectMapper = objectMapper;
		this.timeoutMs = timeoutMs;
		this.fallbackOnAiFailure = fallbackOnAiFailure;
	}
	
	@Override
	public AiValidationResponse validate(Object dto) {
	    try {
	        // Remove null fields before sending to AI
	        String dtoJson = objectMapper.writeValueAsString(dto);
	        Map<String, Object> dtoMap = objectMapper.readValue(
	                dtoJson, new TypeReference<>() {}
	        );
	        dtoMap.values().removeIf(Objects::isNull);

	        String cleanJson = objectMapper.writeValueAsString(dtoMap);
	        String prompt    = buildPrompt(cleanJson, dto);
	        String raw       = callAi(prompt);

	        log.debug("AI raw response: {}", raw);

	        return parseResponse(raw);

	    } catch (Exception e) {
	        return handleFailure(e);
	    }
	}

	private String buildPrompt(String dtoJson, Object dto) {

		String example = buildExample(dto);
		return """
				Validate every field in the following JSON object semantically.

				Rules:
				- Reject random characters, gibberish, offensive or placeholder content
				- Reject values that do not make sense for their field name
				- Accept values that are coherent and contextually appropriate
				- For email fields: check semantic coherence only, not format

				JSON to validate:
				%s

				Respond ONLY with a flat JSON object where:
				- Each key is a field name from the input
				- Each value is "OK" if the field is valid
				- Each value is a short error message if the field is invalid

				Example response structure (your keys must match the input fields):
				%s

				No markdown. No explanation. Only the JSON object.
				""".formatted(sanitize(dtoJson), example);
	}

	private String buildExample(Object dto) {
		String fields = Arrays.stream(dto.getClass().getDeclaredFields()).filter(f -> !f.isSynthetic())
				.filter(f -> !Modifier.isStatic(f.getModifiers()))
				.map(f -> "  \"" + f.getName() + "\": \"OK or error message\"").collect(Collectors.joining(",\n"));

		return "{\n" + fields + "\n}";
	}

	private String callAi(String prompt) {
		try {
			CompletableFuture<String> future = CompletableFuture
					.supplyAsync(() -> chatClient.prompt().user(prompt).call().content());

			String response = future.get(timeoutMs, TimeUnit.MILLISECONDS);

			if (response == null || response.isBlank()) {
				throw new IllegalStateException("AI returned empty response");
			}

			return response;

		} catch (TimeoutException e) {
			throw new RuntimeException("AI timed out after " + timeoutMs + "ms", e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException("AI call interrupted", e);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause().getMessage(), e.getCause());
		}
	}

	private AiValidationResponse parseResponse(String raw) throws Exception {
		String cleaned = raw.substring(raw.indexOf('{'), raw.lastIndexOf('}') + 1);

		Map<String, String> results = objectMapper.readValue(cleaned, new TypeReference<>() {
		});

		Map<String, String> errors = results.entrySet().stream().filter(e -> !"OK".equalsIgnoreCase(e.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		return errors.isEmpty() ? AiValidationResponse.ok() : AiValidationResponse.failed(errors);
	}

	private String sanitize(String value) {
		if (value == null)
			return "";
		return value.replace("\\", "\\\\").replace("\"", "'").trim().substring(0, Math.min(value.length(), 2000));
	}

	private AiValidationResponse handleFailure(Exception e) {
		log.warn("AI validation failed: {}", e.getMessage());

		return fallbackOnAiFailure ? AiValidationResponse.ok()
				: AiValidationResponse
						.failed(Map.of("*", "AI validation service unavailable — please try again later"));
	}
}