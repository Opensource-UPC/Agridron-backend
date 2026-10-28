package pe.com.agridron.shared.infrastructure.web.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Configures CORS globally, independently of the security filter chain.
 *
 * <p>The {@link CorsConfigurationSource} bean is exposed so the IAM bounded context can
 * reuse it with {@code http.cors(Customizer.withDefaults())} once Spring Security is added.
 * The {@link CorsFilter} runs with highest precedence so pre-flight requests are answered
 * before the security filter chain.</p>
 */
@Configuration
public class CorsFilterConfiguration {

    private final List<String> allowedOrigins;
    private final List<String> allowedMethods;
    private final List<String> allowedHeaders;
    private final boolean allowCredentials;
    private final long maxAge;

    public CorsFilterConfiguration(
            @Value("${cors.allowed-origins:http://localhost:4200}") List<String> allowedOrigins,
            @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}") List<String> allowedMethods,
            @Value("${cors.allowed-headers:*}") List<String> allowedHeaders,
            @Value("${cors.allow-credentials:false}") boolean allowCredentials,
            @Value("${cors.max-age:3600}") long maxAge) {
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
        this.allowedHeaders = allowedHeaders;
        this.allowCredentials = allowCredentials;
        this.maxAge = maxAge;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethods);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter(CorsConfigurationSource corsConfigurationSource) {
        return new CorsFilter(corsConfigurationSource);
    }
}
