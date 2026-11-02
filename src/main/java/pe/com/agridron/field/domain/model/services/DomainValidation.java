package pe.com.agridron.field.domain.model.services;

import java.math.BigDecimal;

/**
 * Small validation helpers shared by Field Management aggregates.
 */
public final class DomainValidation {

    private DomainValidation() {
    }

    /**
     * Validates and normalizes a required text field.
     *
     * @param value     raw value
     * @param field     field name used in error messages
     * @param maxLength maximum accepted length after trimming
     * @return the trimmed value
     */
    public static String requireText(String value, String field, int maxLength) {
        if (value == null) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        var normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("%s must not be blank".formatted(field));
        }
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "%s must have at most %d characters".formatted(field, maxLength));
        }
        return normalized;
    }

    /**
     * Validates a required positive identifier.
     */
    public static Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(field));
        }
        return value;
    }

    /**
     * Validates a required positive decimal value.
     */
    public static BigDecimal requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(field));
        }
        return value;
    }
}
