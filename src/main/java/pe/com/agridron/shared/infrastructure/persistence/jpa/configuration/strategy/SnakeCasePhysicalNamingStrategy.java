package pe.com.agridron.shared.infrastructure.persistence.jpa.configuration.strategy;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

import java.util.Locale;

/**
 * Physical naming strategy that maps Java identifiers to {@code snake_case} database
 * identifiers.
 *
 * <p>Table names are intentionally <strong>not</strong> pluralized, so the schema keeps the
 * singular names defined in the AgriDron database design ({@code farm}, {@code parcel},
 * {@code mission}, {@code mission_report}, ...).</p>
 */
public class SnakeCasePhysicalNamingStrategy implements PhysicalNamingStrategy {

    private static final String CAMEL_CASE_BOUNDARY = "([a-z0-9])([A-Z])";
    private static final String ACRONYM_BOUNDARY = "([A-Z]+)([A-Z][a-z])";

    @Override
    public Identifier toPhysicalCatalogName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return null;
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalTableName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toSnakeCase(identifier);
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier identifier, JdbcEnvironment jdbcEnvironment) {
        return toSnakeCase(identifier);
    }

    private Identifier toSnakeCase(Identifier identifier) {
        if (identifier == null) {
            return null;
        }
        var snakeCase = identifier.getText()
                .replaceAll(ACRONYM_BOUNDARY, "$1_$2")
                .replaceAll(CAMEL_CASE_BOUNDARY, "$1_$2")
                .toLowerCase(Locale.ROOT);
        return Identifier.toIdentifier(snakeCase, identifier.isQuoted());
    }
}
