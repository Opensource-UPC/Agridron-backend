package pe.com.agridron.field.domain.model.queries;

/**
 * Requested slice of a collection query.
 *
 * <p>A query can be executed without pagination (the legacy behavior, which returns every
 * matching element) or with an explicit page and size. {@link #paged()} tells the adapter
 * which strategy to use.</p>
 *
 * @param page  zero-based page index
 * @param size  number of elements per page
 * @param paged whether the caller requested pagination
 */
public record Pagination(int page, int size, boolean paged) {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public Pagination {
        if (paged) {
            if (page < 0) {
                throw new IllegalArgumentException("page must not be negative");
            }
            if (size < 1 || size > MAX_SIZE) {
                throw new IllegalArgumentException(
                        "size must be between 1 and " + MAX_SIZE);
            }
        } else if (page != 0 || size != 0) {
            throw new IllegalArgumentException("an unpaged request must use page 0 and size 0");
        }
    }

    /**
     * Creates a paginated request.
     */
    public static Pagination of(int page, int size) {
        return new Pagination(page, size, true);
    }

    /**
     * Creates a request that returns every matching element.
     */
    public static Pagination unpaged() {
        return new Pagination(0, 0, false);
    }
}
