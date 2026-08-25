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
            ReceiptNotFoundException.class, WarrantyNotFoundException.class})
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
        String message = exception.getMessage() != null && !exception.getMessage().isBlank()
                ? exception.getMessage()
                : "Invalid request data or ID";
        return error(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDatabaseError(DataAccessException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "A database error occurred");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError apiError = new ApiError(status.value(), status.getReasonPhrase(), message, Instant.now());
        return ResponseEntity.status(status).body(apiError);
    }
}
