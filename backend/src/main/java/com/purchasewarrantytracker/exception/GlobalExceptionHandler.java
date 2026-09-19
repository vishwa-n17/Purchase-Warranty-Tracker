package com.purchasewarrantytracker.exception;

import java.time.Instant;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ProductNotFoundException.class, PurchaseNotFoundException.class,
            ReceiptNotFoundException.class, WarrantyNotFoundException.class,
            jakarta.persistence.EntityNotFoundException.class})
    public ResponseEntity<ApiError> handleNotFound(RuntimeException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ProductInUseException.class)
    public ResponseEntity<ApiError> handleProductInUse(ProductInUseException exception) {
        return error(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        String safeMessage = "Invalid request data or ID";
        if (exception instanceof MethodArgumentNotValidException validationEx) {
            safeMessage = validationEx.getBindingResult().getFieldErrors().stream()
                    .findFirst()
                    .map(fieldError -> fieldError.getDefaultMessage())
                    .filter(message -> message != null && !message.isBlank())
                    .orElse("Invalid request data");
        } else if (exception instanceof HandlerMethodValidationException handlerEx) {
            safeMessage = handlerEx.getMessage();
            if (safeMessage == null || safeMessage.isBlank()) safeMessage = "Invalid request data";
        }
        return error(HttpStatus.BAD_REQUEST, safeMessage);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseError(DataAccessException ex) {
        String safeMessage = "Something went wrong. Please try again.";
        return error(HttpStatus.INTERNAL_SERVER_ERROR, safeMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        if (ex instanceof org.springframework.web.server.ResponseStatusException statusEx) {
            org.springframework.http.HttpStatus status = org.springframework.http.HttpStatus.valueOf(statusEx.getStatusCode().value());
            String message = statusEx.getReason();
            if (message == null || message.isBlank()) message = status.getReasonPhrase();
            return error(status, message);
        }
        String safeMessage = "Something went wrong. Please try again.";
        return error(HttpStatus.INTERNAL_SERVER_ERROR, safeMessage);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError apiError = new ApiError(status.value(), status.getReasonPhrase(), message, Instant.now());
        return ResponseEntity.status(status).body(apiError);
    }
}
