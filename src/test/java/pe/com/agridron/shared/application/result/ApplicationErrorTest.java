package pe.com.agridron.shared.application.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationErrorTest {

    @Test
    void notFoundBuildsResourceSpecificCode() {
        var error = ApplicationError.notFound("Farm", "42");

        assertThat(error.code()).isEqualTo("FARM_NOT_FOUND");
        assertThat(error.details()).isNull();
    }

    @Test
    void conflictBuildsResourceSpecificCode() {
        var error = ApplicationError.conflict("Farm", "Name already exists");

        assertThat(error.code()).isEqualTo("FARM_CONFLICT");
        assertThat(error.details()).isEqualTo("Name already exists");
    }

    @Test
    void invalidStateBuildsAggregateSpecificCode() {
        assertThat(ApplicationError.invalidState("Mission", "Cannot start").code())
                .isEqualTo("MISSION_INVALID_STATE");
    }

    @Test
    void serviceUnavailableNormalizesServiceName() {
        assertThat(ApplicationError.serviceUnavailable("Weather service", "timeout").code())
                .isEqualTo("WEATHER_SERVICE_UNAVAILABLE");
    }

    @Test
    void securityErrorsUseReservedCodes() {
        assertThat(ApplicationError.unauthorized("missing token").code()).isEqualTo("UNAUTHORIZED");
        assertThat(ApplicationError.invalidCredentials("bad password").code()).isEqualTo("INVALID_CREDENTIALS");
        assertThat(ApplicationError.accessDenied("role required").code()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void unexpectedErrorNeverRequiresDetails() {
        var error = ApplicationError.unexpected("context", null);

        assertThat(error.code()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(error.details()).isNull();
    }
}
