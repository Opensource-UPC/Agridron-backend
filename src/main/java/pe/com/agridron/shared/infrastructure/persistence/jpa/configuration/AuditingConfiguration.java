package pe.com.agridron.shared.infrastructure.persistence.jpa.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * Enables JPA auditing and provides UTC timestamps for {@code @CreatedDate}
 * and {@code @LastModifiedDate} fields.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = AuditingConfiguration.DATE_TIME_PROVIDER_BEAN_NAME)
public class AuditingConfiguration {

    public static final String DATE_TIME_PROVIDER_BEAN_NAME = "auditingDateTimeProvider";

    /**
     * Supplies the current instant for JPA auditing.
     *
     * @return a {@link DateTimeProvider} backed by {@link Instant#now()}
     */
    @Bean(name = DATE_TIME_PROVIDER_BEAN_NAME)
    public DateTimeProvider auditingDateTimeProvider() {
        return () -> Optional.<TemporalAccessor>of(Instant.now());
    }
}
