package pe.com.agridron.field.domain.model.queries;

import java.util.List;
import java.util.function.Function;

/**
 * One page of results plus the metadata needed by clients to navigate the collection.
 *
 * <p>The domain and application layers use this type instead of Spring Data's {@code Page}
 * so repository ports stay independent from the persistence framework.</p>
 *
 * @param content       elements of the page
 * @param page          zero-based page index
 * @param size          requested page size, or the total number of elements for unpaged results
 * @param totalElements total number of matching elements
 * @param <T>           element type
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must not be negative");
        }
    }

    /**
     * Wraps every matching element when the caller did not request pagination.
     */
    public static <T> PageResult<T> unpaged(List<T> content) {
        var elements = List.copyOf(content);
        return new PageResult<>(elements, 0, elements.size(), elements.size());
    }

    /**
     * Total number of pages, or {@code 0} when there are no elements.
     */
    public int totalPages() {
        if (size == 0) {
            return content.isEmpty() ? 0 : 1;
        }
        return (int) ((totalElements + size - 1) / size);
    }

    public boolean first() {
        return page == 0;
    }

    public boolean last() {
        return page + 1 >= totalPages();
    }

    public boolean hasNext() {
        return page + 1 < totalPages();
    }

    /**
     * Maps every element while preserving the page metadata.
     */
    public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
        return new PageResult<>(
                content.stream().<R>map(mapper).toList(),
                page,
                size,
                totalElements);
    }
}
