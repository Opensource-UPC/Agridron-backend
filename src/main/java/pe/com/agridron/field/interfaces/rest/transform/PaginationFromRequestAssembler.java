package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.queries.Pagination;

/**
 * Translates the optional {@code page} and {@code size} query parameters into a
 * {@link Pagination}.
 */
public final class PaginationFromRequestAssembler {

    private PaginationFromRequestAssembler() {
    }

    /**
     * Returns an unpaged request when neither parameter is present. Supplying only one of them
     * enables pagination and uses the default for the other.
     *
     * @param page zero-based page index, or {@code null}
     * @param size page size, or {@code null}
     * @return the pagination request
     */
    public static Pagination toPaginationFromRequest(Integer page, Integer size) {
        if (page == null && size == null) {
            return Pagination.unpaged();
        }
        return Pagination.of(
                page == null ? 0 : page,
                size == null ? Pagination.DEFAULT_SIZE : size);
    }
}
