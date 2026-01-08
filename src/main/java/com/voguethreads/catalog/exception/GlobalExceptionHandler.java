package com.voguethreads.catalog.exception;

import com.voguethreads.catalog.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Product not found - traceId: {}, message: {}", traceId, ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("PRODUCT_NOT_FOUND")
                .message(ex.getMessage())
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSku(DuplicateSkuException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Duplicate SKU - traceId: {}, message: {}", traceId, ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("DUPLICATE_SKU")
                .message(ex.getMessage())
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String traceId = UUID.randomUUID().toString();

        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });

        log.error("Validation error - traceId: {}, details: {}", traceId, details);

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Access denied - traceId: {}, message: {}", traceId, ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("ACCESS_DENIED")
                .message("You do not have permission to perform this action")
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Bad credentials - traceId: {}, message: {}", traceId, ex.getMessage());

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("UNAUTHORIZED")
                .message("Invalid or missing authentication token")
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        String traceId = UUID.randomUUID().toString();
        log.error("Unexpected error - traceId: {}, message: {}", traceId, ex.getMessage(), ex);

        ErrorResponse.ErrorDetail errorDetail = ErrorResponse.ErrorDetail.builder()
                .code("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().error(errorDetail).build());
    }
}

