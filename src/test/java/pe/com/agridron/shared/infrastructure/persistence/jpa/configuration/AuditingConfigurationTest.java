package pe.com.agridron.shared.infrastructure.persistence.jpa.configuration;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AuditingConfigurationTest {

    @Test
    void auditingDateTimeProviderReturnsAnInstant() {
        var configuration = new AuditingConfiguration();

        var now = configuration.auditingDateTimeProvider().getNow();

        assertThat(now).isPresent();
        assertThat(now.orElseThrow()).isInstanceOf(Instant.class);
    }
}
