package com.company.petplatform.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest req) {
    return ResponseEntity.status(ex.getStatus())
        .body(new ApiError(ex.getCode(), ex.getMessage(), req.getHeader("X-Request-Id"), List.of()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    List<String> details = ex.getBindingResult().getAllErrors().stream()
        .map(err -> {
          if (err instanceof FieldError fe) {
            return fe.getField() + ": " + fe.getDefaultMessage();
          }
          return err.getDefaultMessage();
        })
        .toList();
    return ResponseEntity.badRequest()
        .body(new ApiError("VALIDATION_ERROR", "Request validation failed", req.getHeader("X-Request-Id"), details));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex, HttpServletRequest req) {
    List<String> details = ex.getConstraintViolations().stream().map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();
    return ResponseEntity.badRequest()
        .body(new ApiError("VALIDATION_ERROR", "Request validation failed", req.getHeader("X-Request-Id"), details));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiError("SYSTEM_ERROR", ex.getMessage(), req.getHeader("X-Request-Id"), List.of()));
  }
}
