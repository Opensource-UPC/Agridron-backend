package pe.com.agridron.analytics.domain.model.services;

import java.math.BigDecimal;

/**
 * Invariant checks shared by the Analytics aggregates.
 */
public final class DomainValidation {

    private DomainValidation() {
    }

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

    public static String optionalText(String value, String field, int maxLength) {
        if (value == null) {
            return "";
        }
        var normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "%s must have at most %d characters".formatted(field, maxLength));
        }
        return normalized;
    }

    public static Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(field));
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException("%s must not be negative".formatted(field));
        }
        return value;
    }

    public static BigDecimal requireValue(BigDecimal value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        return value;
    }

    public static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        return value;
    }
}
