package pe.com.agridron.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SnakeCasePhysicalNamingStrategyTest {

    private final SnakeCasePhysicalNamingStrategy strategy = new SnakeCasePhysicalNamingStrategy();

    @Test
    void convertsTableNamesToSingularSnakeCase() {
        assertThat(tableName("Farm")).isEqualTo("farm");
        assertThat(tableName("FumigationArea")).isEqualTo("fumigation_area");
        assertThat(tableName("MissionReport")).isEqualTo("mission_report");
        assertThat(tableName("UserAccount")).isEqualTo("user_account");
    }

    @Test
    void doesNotPluralizeTableNames() {
        assertThat(tableName("Farm")).isNotEqualTo("farms");
        assertThat(tableName("Mission")).isNotEqualTo("missions");
    }

    @Test
    void convertsColumnNamesToSnakeCase() {
        assertThat(columnName("createdAt")).isEqualTo("created_at");
        assertThat(columnName("serialNumber")).isEqualTo("serial_number");
        assertThat(columnName("id")).isEqualTo("id");
    }

    @Test
    void handlesAcronyms() {
        assertThat(columnName("APIKey")).isEqualTo("api_key");
        assertThat(tableName("GPSDevice")).isEqualTo("gps_device");
    }

    private String tableName(String name) {
        return strategy.toPhysicalTableName(Identifier.toIdentifier(name), null).getText();
    }

    private String columnName(String name) {
        return strategy.toPhysicalColumnName(Identifier.toIdentifier(name), null).getText();
    }
}
