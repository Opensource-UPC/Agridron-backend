package pe.com.agridron.analytics.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import pe.com.agridron.shared.infrastructure.persistence.jpa.configuration.AuditingConfiguration;
import pe.com.agridron.support.AbstractPostgisIntegrationTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(AuditingConfiguration.class)
public abstract class AbstractAnalyticsPersistenceTest extends AbstractPostgisIntegrationTest {
}
