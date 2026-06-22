package blog.bouguern.smartvalidation.validation;

import java.util.Map;

public record AiValidationResponse(
        Map<String, String> fieldErrors,
        boolean             valid
) {

    public static AiValidationResponse ok() {
        return new AiValidationResponse(Map.of(), true);
    }

    public static AiValidationResponse failed(Map<String, String> fieldErrors) {
        return new AiValidationResponse(Map.copyOf(fieldErrors), false);
    }

    public boolean isValid() {
        return valid;
    }
}