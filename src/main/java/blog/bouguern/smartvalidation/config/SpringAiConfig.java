package blog.bouguern.smartvalidation.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the Spring AI ChatClient bean.
 *
 * Spring AI auto-configures ChatClient.Builder but not ChatClient itself.
 * We build the ChatClient here with a default system prompt that applies
 * to every AI call made through this instance, reducing per-call prompt size.
 */
@Configuration
public class SpringAiConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }


    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a strict semantic validation assistant for a professional API.
                        Always respond with valid JSON only.
                        Never add markdown or any text outside the JSON object.
                        """)
                .build();
    }
}