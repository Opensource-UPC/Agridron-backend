package pe.com.agridron.field.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pe.com.agridron.shared.infrastructure.persistence.jpa.configuration.AuditingConfiguration;
import pe.com.agridron.support.AbstractPostgisIntegrationTest;

/**
 * Base class for Field Management persistence tests.
 *
 * <p>Uses the shared PostGIS container, keeps the real datasource (so Flyway runs) and
 * imports the auditing configuration that {@code @DataJpaTest} does not scan by default.</p>
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
public abstract class AbstractFieldPersistenceTest extends AbstractPostgisIntegrationTest {
}
