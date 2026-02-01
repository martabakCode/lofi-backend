package com.lofi.lofiapps.exception;

import com.lofi.lofiapps.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  private final org.springframework.core.env.Environment env;

  public GlobalExceptionHandler(org.springframework.core.env.Environment env) {
    this.env = env;
  }

  private boolean isDev() {
    return java.util.Arrays.asList(env.getActiveProfiles()).contains("dev");
  }

  private Object getDebugError(Exception ex) {
    if (isDev()) {
      Map<String, String> debugInfo = new HashMap<>();
      debugInfo.put("exception", ex.getClass().getName());
      debugInfo.put("message", ex.getMessage());
      java.io.StringWriter sw = new java.io.StringWriter();
      java.io.PrintWriter pw = new java.io.PrintWriter(sw);
      ex.printStackTrace(pw);
      debugInfo.put("stackTrace", sw.toString());
      return debugInfo;
    }
    return null;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
      ResourceNotFoundException ex) {
    log.error("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("NOT_FOUND", ex.getMessage(), getDebugError(ex)));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });

    log.error("Validation error: {}", errors);

    return ResponseEntity.badRequest()
        .body(ApiResponse.error("VALIDATION_ERROR", "Invalid request", errors));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
      BadCredentialsException ex) {
    log.error("Bad credentials: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("UNAUTHORIZED", "Invalid email or password", getDebugError(ex)));
  }

  @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
  public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(RuntimeException ex) {
    log.error("Access denied: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ApiResponse.error("FORBIDDEN", ex.getMessage(), getDebugError(ex)));
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiResponse<Object>> handleBadRequestExceptions(RuntimeException ex) {
    log.error("Bad request: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("BAD_REQUEST", ex.getMessage(), getDebugError(ex)));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Object>> handleConstraintViolationException(
      ConstraintViolationException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation -> {
              String propertyPath = violation.getPropertyPath().toString();
              errors.put(propertyPath, violation.getMessage());
            });
    log.error("Constraint violation: {}", errors);
    return ResponseEntity.badRequest()
        .body(ApiResponse.error("VALIDATION_ERROR", "Data validation failed", errors));
  }

  @ExceptionHandler(TransactionSystemException.class)
  public ResponseEntity<ApiResponse<Object>> handleTransactionSystemException(
      TransactionSystemException ex) {
    Throwable cause = ex.getRootCause();
    if (cause instanceof ConstraintViolationException) {
      return handleConstraintViolationException((ConstraintViolationException) cause);
    }
    log.error("Transaction system error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("TRANSACTION_ERROR", "Transaction failed", getDebugError(ex)));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Object>> handleAllExceptions(Exception ex, WebRequest request) {
    log.error("Unexpected error occurred", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiResponse.error(
                "INTERNAL_SERVER_ERROR", "An unexpected error occurred", getDebugError(ex)));
  }
}
