package pe.com.agridron.shared.infrastructure.web.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsFilterConfigurationTest {

    @Test
    void exposesConfiguredCorsPolicy() {
        var configuration = new CorsFilterConfiguration(
                List.of("http://localhost:4200"),
                List.of("GET", "POST"),
                List.of("*"),
                false,
                3600);

        var source = configuration.corsConfigurationSource();
        var corsConfiguration = source.getCorsConfiguration(
                new MockHttpServletRequest("GET", "/api/v1/status"));

        assertThat(corsConfiguration).isNotNull();
        assertThat(corsConfiguration.getAllowedOrigins()).containsExactly("http://localhost:4200");
        assertThat(corsConfiguration.getAllowedMethods()).containsExactly("GET", "POST");
        assertThat(corsConfiguration.getAllowCredentials()).isFalse();
        assertThat(corsConfiguration.getMaxAge()).isEqualTo(3600L);
    }
}
