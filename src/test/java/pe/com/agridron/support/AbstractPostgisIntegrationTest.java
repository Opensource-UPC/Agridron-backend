package pe.com.agridron.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests that need a real PostgreSQL instance with PostGIS.
 *
 * <p>The container is static so it is started once per JVM and shared by every test class
 * that extends this base. Subclasses that use Spring slices must disable the replacement of
 * the datasource, for example with {@code @AutoConfigureTestDatabase(replace = NONE)}.</p>
 */
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractPostgisIntegrationTest {

    private static final DockerImageName POSTGIS_IMAGE = DockerImageName
            .parse("postgis/postgis:17-3.5")
            .asCompatibleSubstituteFor("postgres");

    @Container
    protected static final PostgreSQLContainer POSTGIS =
            new PostgreSQLContainer(POSTGIS_IMAGE)
                    .withDatabaseName("agridron")
                    .withUsername("agridron")
                    .withPassword("agridron");

    @DynamicPropertySource
    static void registerPostgisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGIS::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGIS::getUsername);
        registry.add("spring.datasource.password", POSTGIS::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGIS::getDriverClassName);
    }
}
