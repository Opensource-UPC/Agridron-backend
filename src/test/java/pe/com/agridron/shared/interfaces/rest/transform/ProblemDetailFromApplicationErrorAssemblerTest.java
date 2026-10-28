package pe.com.agridron.shared.interfaces.rest.transform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.net.URI;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProblemDetailFromApplicationErrorAssemblerTest {

    @AfterEach
    void clearLocale() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void mapsNotFoundErrorsTo404() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.notFound("Farm", "42"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(404, response.getStatusCode().value());
        assertEquals(404, problemDetail.getStatus());
        assertEquals("FARM_NOT_FOUND", problemDetail.getProperties().get("code"));
        assertEquals("Resource not found", problemDetail.getDetail());
        assertEquals("Resource Not Found", problemDetail.getTitle());
        assertEquals(URI.create("/api/v1/errors/not-found"), problemDetail.getType());
    }

    @Test
    void mapsConflictErrorsTo409InSpanish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.conflict("User", "Username already exists"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(409, response.getStatusCode().value());
        assertEquals("USER_CONFLICT", problemDetail.getProperties().get("code"));
        assertEquals("Conflicto de recurso", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/conflict"), problemDetail.getType());
    }

    @Test
    void mapsUnauthorizedErrorsTo401() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.invalidCredentials("wrong password"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("INVALID_CREDENTIALS", problemDetail.getProperties().get("code"));
        assertEquals("Authentication required", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/unauthorized"), problemDetail.getType());
    }

    @Test
    void mapsAccessDeniedErrorsTo403() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.accessDenied("Missing role"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(403, response.getStatusCode().value());
        assertEquals("Acceso denegado", problemDetail.getDetail());
        assertEquals(URI.create("/api/v1/errors/forbidden"), problemDetail.getType());
    }

    @Test
    void mapsBusinessRuleViolationsTo422() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.invalidState("Mission", "Cannot start an unauthorized mission"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(422, response.getStatusCode().value());
        assertEquals("MISSION_INVALID_STATE", problemDetail.getProperties().get("code"));
        assertEquals("Business rule violation", problemDetail.getDetail());
    }

    @Test
    void mapsUnavailableServicesTo503() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.serviceUnavailable("Weather service", "timeout"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(503, response.getStatusCode().value());
        assertEquals("WEATHER_SERVICE_UNAVAILABLE", problemDetail.getProperties().get("code"));
        assertEquals(URI.create("/api/v1/errors/service-unavailable"), problemDetail.getType());
        assertEquals("Service temporarily unavailable", problemDetail.getDetail());
    }

    @Test
    void mapsUnexpectedErrorsTo500AndKeepsOptionalDetails() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.unexpected("context", "internal detail"));
        var problemDetail = response.getBody();

        assertNotNull(problemDetail);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", problemDetail.getDetail());
        assertEquals("internal detail", problemDetail.getProperties().get("details"));
        assertEquals(URI.create("/api/v1/errors/unexpected-error"), problemDetail.getType());
    }

    @Test
    void omitsDetailsPropertyWhenDetailsAreNull() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);

        var response = ProblemDetailFromApplicationErrorAssembler.toResponseFromApplicationError(
                ApplicationError.unexpected("context", null));

        assertNotNull(response.getBody());
        assertNull(response.getBody().getProperties().get("details"));
    }
}
