package pe.com.agridron.support;

import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractPostgisIntegrationTestTest extends AbstractPostgisIntegrationTest {

    @Test
    void startsAPostgisEnabledContainer() throws Exception {
        try (var connection = DriverManager.getConnection(
                POSTGIS.getJdbcUrl(), POSTGIS.getUsername(), POSTGIS.getPassword());
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT PostGIS_Version()")) {
            assertTrue(resultSet.next(), "PostGIS_Version() must return a row");
            assertFalse(resultSet.getString(1).isBlank(), "PostGIS version must not be blank");
        }
    }
}
