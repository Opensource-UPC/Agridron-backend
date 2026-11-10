package pe.com.agridron.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Flyway migration strategy that performs a repair before migrating.
 *
 * <p>Ensures that any rolled back or failed migration entries in the schema history
 * are repaired cleanly before attempting migrations.</p>
 */
@Configuration
public class FlywayConfiguration {

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            flyway.repair();
            flyway.migrate();
        };
    }
}
