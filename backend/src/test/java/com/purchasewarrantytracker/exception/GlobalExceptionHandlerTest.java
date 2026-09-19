package com.purchasewarrantytracker.exception;

import com.purchasewarrantytracker.exception.ProductInUseException;
import com.purchasewarrantytracker.exception.ProductNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Database error returns generic safe message without SQL details")
    void databaseErrorReturnsSafeMessage() {
        DataAccessException ex = new org.springframework.jdbc.BadSqlGrammarException(
                "bad SQL", "SELECT id FROM users WHERE email = ?", new java.sql.SQLException("SQL syntax error")
        );

        ResponseEntity<ApiError> response = handler.handleDatabaseError(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.message()).isEqualTo("Something went wrong. Please try again.");
        assertThat(body.message()).doesNotContain("SELECT", "SQL", "users", "PreparedStatement");
    }

    @Test
    @DisplayName("Unexpected generic exception returns safe generic message")
    void unexpectedExceptionReturnsSafeMessage() {
        Exception ex = new NullPointerException("Something broke internally");

        ResponseEntity<ApiError> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(500);
        assertThat(body.message()).isEqualTo("Something went wrong. Please try again.");
        assertThat(body.message()).doesNotContain("NullPointerException", "Something broke");
    }

    @Test
    @DisplayName("ResponseStatusException is propagated with its original status")
    void responseStatusExceptionIsPropagated() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        ResponseEntity<ApiError> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(401);
    }

    @Test
    @DisplayName("Not found exception returns safe message without internal details")
    void notFoundReturnsSafeMessage() {
        ProductNotFoundException ex = new ProductNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(404);
        assertThat(body.message()).contains("Product"); // custom message is OK
    }

    @Test
    @DisplayName("Product in use conflict returns safe message")
    void productInUseReturnsSafeMessage() {
        ProductInUseException ex = new ProductInUseException(1L);

        ResponseEntity<ApiError> response = handler.handleProductInUse(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(409);
    }
}
