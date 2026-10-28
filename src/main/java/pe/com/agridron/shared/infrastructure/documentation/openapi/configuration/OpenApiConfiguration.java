package pe.com.agridron.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI specification exposed by the platform.
 */
@Configuration
public class OpenApiConfiguration {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Value("${documentation.application.title:AgriDron API}")
    private String applicationTitle;

    @Value("${documentation.application.description:AgriDron Solutions backend API}")
    private String applicationDescription;

    @Value("${documentation.application.version:0.1.0}")
    private String applicationVersion;

    @Bean
    public OpenAPI agridronOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(applicationTitle)
                        .description(applicationDescription)
                        .version(applicationVersion)
                        .contact(new Contact()
                                .name("AgriDron Solutions")
                                .email("support@agridron.com")
                                .url("https://agridron.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Environment"),
                        new Server().url("https://staging-api.agridron.com").description("Staging Environment"),
                        new Server().url("https://api.agridron.com").description("Production Environment")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Bearer token for API authentication")));
    }
}
