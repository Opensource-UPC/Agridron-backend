package pe.com.agridron.shared.interfaces.rest.transform;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Assembler that transforms {@link ApplicationError} instances into RFC 9457
 * {@link ProblemDetail} responses.
 *
 * <p>The HTTP status is derived from the error code suffix/prefix, the {@code type} URI is
 * built from the same code and the {@code detail} is resolved from the i18n bundles.</p>
 */
@NullMarked
public final class ProblemDetailFromApplicationErrorAssembler {

    private static final String MESSAGES_BASENAME = "messages";
    private static final String ERROR_TYPE_PREFIX = "/api/v1/errors/";

    private ProblemDetailFromApplicationErrorAssembler() {
    }

    /**
     * Transforms an {@link ApplicationError} into a Spring {@link ProblemDetail}.
     *
     * @param error the application error to transform
     * @return the assembled {@link ProblemDetail}
     */
    public static ProblemDetail toProblemDetailFromApplicationError(ApplicationError error) {
        var status = toStatusFromErrorCode(error.code());
        var localizedMessage = toLocalizedMessageFromApplicationError(error);

        var problemDetail = ProblemDetail.forStatusAndDetail(status, localizedMessage);
        problemDetail.setType(toProblemTypeUri(error.code()));
        problemDetail.setTitle(toProblemTitle(error.code(), status));
        problemDetail.setProperty("code", error.code());

        if (error.details() != null && !error.details().isBlank()) {
            problemDetail.setProperty("details", error.details());
        }

        return problemDetail;
    }

    /**
     * Transforms an {@link ApplicationError} into a {@link ResponseEntity} containing a
     * {@link ProblemDetail}.
     *
     * @param error the application error to transform
     * @return response entity with the corresponding HTTP status and ProblemDetail body
     */
    public static ResponseEntity<ProblemDetail> toResponseFromApplicationError(ApplicationError error) {
        var problemDetail = toProblemDetailFromApplicationError(error);
        return ResponseEntity.status(problemDetail.getStatus()).body(problemDetail);
    }

    /**
     * Determines the appropriate HTTP status code for a given error code.
     *
     * @param errorCode the machine-readable error code
     * @return the corresponding {@link HttpStatusCode}
     */
    public static HttpStatusCode toStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR", "INVALID_INPUT", "MALFORMED_REQUEST", "INVALID_GEOMETRY" ->
                    HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED", "INVALID_CREDENTIALS", "TOKEN_EXPIRED", "TOKEN_INVALID" ->
                    HttpStatus.UNAUTHORIZED;
            case "ACCESS_DENIED", "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "METHOD_NOT_ALLOWED" -> HttpStatus.METHOD_NOT_ALLOWED;
            case "UNSUPPORTED_MEDIA_TYPE" -> HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            case "NOT_ACCEPTABLE" -> HttpStatus.NOT_ACCEPTABLE;
            case String s when s.endsWith("_NOT_FOUND") -> HttpStatus.NOT_FOUND;
            case String s when s.endsWith("_CONFLICT") || s.endsWith("_ALREADY_EXISTS") ->
                    HttpStatus.CONFLICT;
            case String s when s.endsWith("_UNAVAILABLE") || s.equals("SERVICE_UNAVAILABLE") ->
                    HttpStatus.SERVICE_UNAVAILABLE;
            case String s when s.endsWith("_INVALID_STATE")
                    || s.endsWith("_NOT_AVAILABLE")
                    || s.endsWith("_NOT_SUITABLE")
                    || s.endsWith("_INSUFFICIENT")
                    || s.startsWith("INSUFFICIENT_")
                    || s.equals("BUSINESS_RULE_VIOLATION") -> HttpStatus.UNPROCESSABLE_CONTENT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static URI toProblemTypeUri(String errorCode) {
        var slug = switch (errorCode) {
            case "VALIDATION_ERROR" -> "validation-error";
            case "INVALID_GEOMETRY" -> "invalid-geometry";
            case "BUSINESS_RULE_VIOLATION" -> "business-rule-violation";
            case "UNEXPECTED_ERROR" -> "unexpected-error";
            case "UNAUTHORIZED", "INVALID_CREDENTIALS", "TOKEN_EXPIRED", "TOKEN_INVALID" -> "unauthorized";
            case "ACCESS_DENIED", "FORBIDDEN" -> "forbidden";
            case String s when s.endsWith("_NOT_FOUND") -> "not-found";
            case String s when s.endsWith("_CONFLICT") || s.endsWith("_ALREADY_EXISTS") -> "conflict";
            case String s when s.endsWith("_UNAVAILABLE") || s.equals("SERVICE_UNAVAILABLE") ->
                    "service-unavailable";
            default -> errorCode.toLowerCase(Locale.ROOT).replace('_', '-');
        };
        return URI.create(ERROR_TYPE_PREFIX + slug);
    }

    private static String toProblemTitle(String errorCode, HttpStatusCode status) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> "Validation Error";
            case "INVALID_GEOMETRY" -> "Invalid Geometry";
            case "BUSINESS_RULE_VIOLATION" -> "Business Rule Violation";
            case "UNAUTHORIZED", "INVALID_CREDENTIALS", "TOKEN_EXPIRED", "TOKEN_INVALID" -> "Unauthorized";
            case "ACCESS_DENIED", "FORBIDDEN" -> "Forbidden";
            case String s when s.endsWith("_NOT_FOUND") -> "Resource Not Found";
            case String s when s.endsWith("_CONFLICT") || s.endsWith("_ALREADY_EXISTS") -> "Resource Conflict";
            case String s when s.endsWith("_UNAVAILABLE") || s.equals("SERVICE_UNAVAILABLE") ->
                    "Service Unavailable";
            default -> status instanceof HttpStatus httpStatus
                    ? httpStatus.getReasonPhrase()
                    : "Error";
        };
    }

    private static String toLocalizedMessageFromApplicationError(ApplicationError error) {
        var specificKey = toSpecificMessageKeyFromErrorCode(error.code());
        var specificMessage = toLocalizedMessageOrNull(
                specificKey,
                error.details(),
                toEntityNameFromErrorCode(error.code()));
        if (specificMessage != null) {
            return specificMessage;
        }

        var fallbackKey = toMessageKeyFromErrorCode(error.code());
        return toLocalizedMessageWithFallback(
                fallbackKey,
                error.message(),
                error.details(),
                toEntityNameFromErrorCode(error.code()));
    }

    private static String toSpecificMessageKeyFromErrorCode(String errorCode) {
        return "error.%s.message".formatted(errorCode.toLowerCase(Locale.ROOT).replace('_', '-'));
    }

    private static String toMessageKeyFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "VALIDATION_ERROR" -> "error.validation.message";
            case "BUSINESS_RULE_VIOLATION" -> "error.business-rule.message";
            case "UNAUTHORIZED", "INVALID_CREDENTIALS", "TOKEN_EXPIRED", "TOKEN_INVALID" ->
                    "error.unauthorized.message";
            case "ACCESS_DENIED", "FORBIDDEN" -> "error.forbidden.message";
            case "UNEXPECTED_ERROR" -> "error.unexpected.message";
            case String s when s.endsWith("_NOT_FOUND") -> "error.not-found.message";
            case String s when s.endsWith("_CONFLICT") || s.endsWith("_ALREADY_EXISTS") ->
                    "error.conflict.message";
            case String s when s.endsWith("_UNAVAILABLE") || s.equals("SERVICE_UNAVAILABLE") ->
                    "error.service-unavailable.message";
            case String s when s.endsWith("_INVALID_STATE") -> "error.business-rule.message";
            default -> "error.generic.message";
        };
    }

    private static String toEntityNameFromErrorCode(String errorCode) {
        if (errorCode.endsWith("_NOT_FOUND")) {
            return errorCode.replace("_NOT_FOUND", "").toLowerCase(Locale.ROOT);
        }
        if (errorCode.endsWith("_CONFLICT")) {
            return errorCode.replace("_CONFLICT", "").toLowerCase(Locale.ROOT);
        }
        return "resource";
    }

    private static @Nullable String toLocalizedMessageOrNull(String key, Object... args) {
        var locale = LocaleContextHolder.getLocale();
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            if (!bundle.containsKey(key)) {
                return null;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return null;
        }
    }

    private static String toLocalizedMessageWithFallback(String key, String fallback, Object... args) {
        var locale = LocaleContextHolder.getLocale();
        try {
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            if (!bundle.containsKey(key)) {
                return fallback;
            }
            return MessageFormat.format(bundle.getString(key), args);
        } catch (MissingResourceException ex) {
            return fallback;
        }
    }
}
