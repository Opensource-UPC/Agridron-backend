

package pe.com.agridron.inventory.domain.model.service;

/**
 * Validaciones pequeñas que comparten los agregados de Inventory & Resource Management.
 *
 * <p>Es la última barrera del dominio: si algo llega inválido hasta aquí, se lanza una
 * excepción. Las reglas de negocio se revisan antes en los command services y se devuelven
 * como {@code Result.failure(...)}.</p>
 */
public final class DomainValidation {

    private DomainValidation() {
    }

    /**
     * Valida un texto obligatorio y lo devuelve sin espacios al inicio ni al final.
     */
    public static String requireText(String value, String field, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("%s is required".formatted(field));
        }
        var normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(
                    "%s must have at most %d characters".formatted(field, maxLength));
        }
        return normalized;
    }

    /**
     * Valida un identificador obligatorio y positivo.
     */
    public static Long requirePositive(Long value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(field));
        }
        return value;
    }

    /**
     * Valida un decimal obligatorio y positivo (litros, milímetros, etc.).
     */
    public static Double requirePositive(Double value, String field) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("%s must be positive".formatted(field));
        }
        return value;
    }

    /**
     * Valida un entero que no puede ser negativo.
     */
    public static int requireNonNegative(int value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException("%s must not be negative".formatted(field));
        }
        return value;
    }

    /**
     * Valida un entero dentro de un rango cerrado [min, max].
     */
    public static int requireBetween(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    "%s must be between %d and %d".formatted(field, min, max));
        }
        return value;
    }

    /**
     * Devuelve el texto sin espacios o {@code null} si viene vacío (para campos opcionales).
     */
    public static String optionalText(String value, int maxLength, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requireText(value, field, maxLength);
    }
}