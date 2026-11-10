package pe.com.agridron.field.interfaces.rest.resources;

import pe.com.agridron.field.domain.model.queries.PageResult;

import java.util.List;

/**
 * Paginated response envelope used when the caller supplies {@code page} or {@code size}.
 *
 * @param content       elements of the page
 * @param page          zero-based page index
 * @param size          requested page size
 * @param totalElements total number of matching elements
 * @param totalPages    total number of pages
 * @param first         whether this is the first page
 * @param last          whether this is the last page
 * @param <T>           element type
 */
public record PageResource<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public PageResource {
        content = List.copyOf(content);
    }

    /**
     * Builds the envelope from an application-level page result.
     */
    public static <T> PageResource<T> from(PageResult<T> pageResult) {
        return new PageResource<>(
                pageResult.content(),
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.first(),
                pageResult.last());
    }
}
