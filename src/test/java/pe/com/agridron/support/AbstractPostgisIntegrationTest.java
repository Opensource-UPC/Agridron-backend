package pe.com.agridron.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that need a real PostgreSQL instance with PostGIS.
 *
 * <p>Uses the singleton-container pattern: the container is started once per JVM and is
 * stopped by the Testcontainers resource reaper when the JVM exits. This keeps every
 * Spring context pointing at the same live database; a container bound to the JUnit
 * lifecycle would be stopped between test classes and cached contexts would keep the
 * old, now closed port.</p>
 *
 * <p>Subclasses that use Spring slices must disable the replacement of the datasource,
 * for example with {@code @AutoConfigureTestDatabase(replace = NONE)}.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractPostgisIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:17-3.5")
            .asCompatibleSubstituteFor("postgres");

    protected static final PostgreSQLContainer POSTGIS =
            new PostgreSQLContainer(POSTGIS_IMAGE)
                    .withDatabaseName("agridron")
                    .withUsername("agridron")
                    .withPassword("agridron");

    static {
        POSTGIS.start();
    }

    @DynamicPropertySource
    static void registerPostgisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGIS::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGIS::getUsername);
        registry.add("spring.datasource.password", POSTGIS::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGIS::getDriverClassName);
    }
}
