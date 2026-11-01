package pe.com.agridron.field.domain.model.queries;

/**
 * Ordering requested for a collection query.
 *
 * @param field property exposed by the endpoint
 * @param direction sort direction
 */
public record SortOrder(String field, SortDirection direction) {

    public SortOrder {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("sort field must not be blank");
        }
        if (direction == null) {
            throw new IllegalArgumentException("sort direction must not be null");
        }
        field = field.trim();
    }
}
