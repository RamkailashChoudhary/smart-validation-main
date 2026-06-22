package blog.bouguern.smartvalidation.exception;

import blog.bouguern.smartvalidation.validation.AiValidationResponse;
import lombok.Getter;

@Getter
public class AiValidationException extends RuntimeException {

    private final AiValidationResponse validationResponse;

    public AiValidationException(AiValidationResponse validationResponse) {
        super("AI semantic validation failed");
        this.validationResponse = validationResponse;
    }
}