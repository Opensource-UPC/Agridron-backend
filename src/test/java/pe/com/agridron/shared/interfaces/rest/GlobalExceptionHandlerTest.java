package pe.com.agridron.shared.interfaces.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void handleRuntimeExceptionDoesNotLeakInternalMessages() {
        var response = handler.handleRuntimeException(new RuntimeException("database password leaked"));
        var problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals(500, response.getStatusCode().value());
        assertEquals(500, problemDetail.getStatus());
        assertEquals("UNEXPECTED_ERROR", problemDetail.getProperties().get("code"));
        assertEquals("Unexpected error", problemDetail.getDetail());
        assertNull(problemDetail.getProperties().get("details"));
        assertEquals(URI.create("/api/v1/errors/unexpected-error"), problemDetail.getType());
    }

    @Test
    void handleRuntimeExceptionUsesSpanishBundle() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var response = handler.handleRuntimeException(new RuntimeException("boom"));
        var problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals("Error inesperado", problemDetail.getDetail());
    }

    @Test
    void handleIllegalArgumentExceptionReturnsValidationError() {
        var response = handler.handleIllegalArgumentException(
                new IllegalArgumentException("Farm id must be a valid UUID"));
        var problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("VALIDATION_ERROR", problemDetail.getProperties().get("code"));
        assertEquals("Validation failed", problemDetail.getDetail());
        assertEquals("Farm id must be a valid UUID", problemDetail.getProperties().get("details"));
        assertEquals(URI.create("/api/v1/errors/validation-error"), problemDetail.getType());
    }

    @Test
    void handleExceptionFallsBackToUnexpectedError() {
        var response = handler.handleException(new Exception("checked failure"));
        ProblemDetail problemDetail = Objects.requireNonNull(response.getBody());

        assertEquals(500, response.getStatusCode().value());
        assertEquals("UNEXPECTED_ERROR", problemDetail.getProperties().get("code"));
        assertNull(problemDetail.getProperties().get("details"));
    }
}
