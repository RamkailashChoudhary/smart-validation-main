package blog.bouguern.smartvalidation.exception;

import java.time.Instant;
import java.util.List;

public record ValidationErrorResponse(
        Instant          timestamp,
        int              status,
        String           error,
        String           path,
        List<FieldError> fieldErrors
) {
    public record FieldError(
            String field,
            String message
    ) {}
}