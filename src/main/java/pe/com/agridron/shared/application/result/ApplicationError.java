package pe.com.agridron.shared.application.result;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

/**
 * Represents an error that occurred in the application layer.
 *
 * <p>The {@code code} is machine-readable and drives both the HTTP status mapping and the
 * RFC 9457 {@code type} URI. The {@code message} is a human-readable fallback, while
 * {@code details} adds optional context.</p>
 *
 * @param code    machine-readable error code (e.g. {@code FARM_NOT_FOUND}, {@code ACCESS_DENIED})
 * @param message human-readable error message
 * @param details optional additional context about the error
 */
@NullMarked
public record ApplicationError(
        String code,
        String message,
        @Nullable String details) {

    /**
     * Creates an application error with code and message only.
     */
    public ApplicationError(String code, String message) {
        this(code, message, null);
    }

    /**
     * Validation error: input data is invalid or violates constraints.
     */
    public static ApplicationError validationError(String fieldOrConcept, String reason) {
        return new ApplicationError(
                "VALIDATION_ERROR",
                "Validation failed: %s".formatted(fieldOrConcept),
                reason);
    }

    /**
     * Invalid geometry error: a spatial value does not satisfy the geometry invariants.
     */
    public static ApplicationError invalidGeometry(String reason) {
        return new ApplicationError("INVALID_GEOMETRY", "Invalid geometry", reason);
    }

    /**
     * Not found error: the requested resource does not exist.
     */
    public static ApplicationError notFound(String resourceType, String identifier) {
        return new ApplicationError(
                codeFor(resourceType, "NOT_FOUND"),
                "%s not found: %s".formatted(resourceType, identifier),
                null);
    }

    /**
     * Conflict error: the operation cannot be completed due to conflicting state.
     */
    public static ApplicationError conflict(String resource, String reason) {
        return new ApplicationError(
                codeFor(resource, "CONFLICT"),
                "Conflict with %s".formatted(resource),
                reason);
    }

    /**
     * Business rule violation: the operation violates a domain invariant.
     */
    public static ApplicationError businessRuleViolation(String rule, String reason) {
        return new ApplicationError(
                "BUSINESS_RULE_VIOLATION",
                "Business rule violation: %s".formatted(rule),
                reason);
    }

    /**
     * Invalid state transition for an aggregate.
     */
    public static ApplicationError invalidState(String aggregate, String reason) {
        return new ApplicationError(
                codeFor(aggregate, "INVALID_STATE"),
                "Invalid state for %s".formatted(aggregate),
                reason);
    }

    /**
     * Authentication is required.
     */
    public static ApplicationError unauthorized(String reason) {
        return new ApplicationError("UNAUTHORIZED", "Authentication required", reason);
    }

    /**
     * The supplied credentials are invalid.
     */
    public static ApplicationError invalidCredentials(String reason) {
        return new ApplicationError("INVALID_CREDENTIALS", "Invalid credentials", reason);
    }

    /**
     * The authenticated principal is not allowed to perform the operation.
     */
    public static ApplicationError accessDenied(String reason) {
        return new ApplicationError("ACCESS_DENIED", "Access denied", reason);
    }

    /**
     * An external or internal dependency is temporarily unavailable.
     */
    public static ApplicationError serviceUnavailable(String service, String reason) {
        return new ApplicationError(
                codeFor(service, "UNAVAILABLE"),
                "%s is temporarily unavailable".formatted(service),
                reason);
    }

    /**
     * The HTTP method is not supported for the requested resource.
     */
    public static ApplicationError methodNotAllowed(String reason) {
        return new ApplicationError("METHOD_NOT_ALLOWED", "Method not allowed", reason);
    }

    /**
     * The request media type is not supported.
     */
    public static ApplicationError unsupportedMediaType(String reason) {
        return new ApplicationError("UNSUPPORTED_MEDIA_TYPE", "Unsupported media type", reason);
    }

    /**
     * Unexpected error: something went wrong that should not have happened.
     */
    public static ApplicationError unexpected(String context, @Nullable String reason) {
        return new ApplicationError(
                "UNEXPECTED_ERROR",
                "Unexpected error in %s".formatted(context),
                reason);
    }

    private static String codeFor(String name, String suffix) {
        return "%s_%s".formatted(toCodeSegment(name), suffix);
    }

    private static String toCodeSegment(String value) {
        return value.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
