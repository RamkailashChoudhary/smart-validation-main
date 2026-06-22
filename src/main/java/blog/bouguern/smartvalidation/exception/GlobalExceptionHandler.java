package blog.bouguern.smartvalidation.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleAiValidation(
            AiValidationException ex,
            HttpServletRequest request) {

        log.warn("AI validation failed for [{}]", request.getRequestURI());

        List<ValidationErrorResponse.FieldError> fieldErrors =
                ex.getValidationResponse().fieldErrors().entrySet().stream()
                        .map(e -> new ValidationErrorResponse.FieldError(e.getKey(), e.getValue()))
                        .toList();

        return ResponseEntity
                .unprocessableEntity()
                .body(new ValidationErrorResponse(
                        Instant.now(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value(),
                        "Semantic validation failed",
                        request.getRequestURI(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleBeanValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ValidationErrorResponse.FieldError> fieldErrors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(e -> new ValidationErrorResponse.FieldError(
                                e.getField(), e.getDefaultMessage()
                        ))
                        .toList();

        return ResponseEntity
                .badRequest()
                .body(new ValidationErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "Validation failed",
                        request.getRequestURI(),
                        fieldErrors
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unexpected error [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .internalServerError()
                .body(new ValidationErrorResponse(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "An unexpected error occurred",
                        request.getRequestURI(),
                        List.of()
                ));
    }
}