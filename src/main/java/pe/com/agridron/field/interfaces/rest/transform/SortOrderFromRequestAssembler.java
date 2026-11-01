package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;

import java.util.Locale;
import java.util.Set;

/**
 * Translates the optional {@code sort} query parameter into a {@link SortOrder}.
 *
 * <p>The accepted syntax is {@code field} or {@code field,asc|desc}. Only the fields registered
 * as sortable by the endpoint are accepted.</p>
 */
public final class SortOrderFromRequestAssembler {

    private SortOrderFromRequestAssembler() {
    }

    /**
     * Parses the raw sort expression.
     *
     * @param sort          raw value, or {@code null} for the endpoint default
     * @param allowedFields fields the endpoint exposes for ordering
     * @return the sort order, or {@code null} when no value was supplied
     */
    public static SortOrder toSortOrderFromRequest(String sort, Set<String> allowedFields) {
        if (sort == null || sort.isBlank()) {
            return null;
        }

        var parts = sort.split(",", -1);
        if (parts.length > 2) {
            throw new IllegalArgumentException(
                    "sort must use the format 'field' or 'field,asc|desc'");
        }

        var field = parts[0].trim().toLowerCase(Locale.ROOT);
        if (!allowedFields.contains(field)) {
            throw new IllegalArgumentException(
                    "sort field '%s' is not supported; allowed fields: %s"
                            .formatted(field, allowedFields.stream().sorted().toList()));
        }

        var direction = parts.length == 2
                ? parseDirection(parts[1])
                : SortDirection.ASC;
        return new SortOrder(field, direction);
    }

    private static SortDirection parseDirection(String raw) {
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> SortDirection.ASC;
            case "desc" -> SortDirection.DESC;
            default -> throw new IllegalArgumentException(
                    "sort direction must be 'asc' or 'desc'");
        };
    }
}
