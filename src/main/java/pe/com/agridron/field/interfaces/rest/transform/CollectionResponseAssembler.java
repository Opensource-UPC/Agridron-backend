package pe.com.agridron.field.interfaces.rest.transform;

import org.springframework.http.ResponseEntity;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.interfaces.rest.resources.PageResource;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;
import pe.com.agridron.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;

import java.util.function.Function;

/**
 * Builds collection responses while preserving the legacy contract.
 *
 * <p>Requests without pagination keep returning a JSON array. When the caller supplies
 * {@code page} or {@code size}, the response becomes a {@link PageResource} envelope.</p>
 */
public final class CollectionResponseAssembler {

    private CollectionResponseAssembler() {
    }

    /**
     * Maps a page result to an array or to a paginated envelope.
     *
     * @param pageResult the page result
     * @param paged      whether the request asked for pagination
     * @param mapper     resource assembler
     * @param <T>        domain type
     * @param <R>        resource type
     * @return the HTTP response
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromPageResult(
            PageResult<T> pageResult,
            boolean paged,
            Function<T, R> mapper) {
        if (!paged) {
            return ResponseEntity.ok(pageResult.content().stream().map(mapper).toList());
        }
        return ResponseEntity.ok(PageResource.from(pageResult.map(mapper)));
    }

    /**
     * Maps a successful page result, or delegates the failure to the shared problem-detail
     * assembler.
     *
     * @param result the application result
     * @param paged  whether the request asked for pagination
     * @param mapper resource assembler
     * @param <T>    domain type
     * @param <R>    resource type
     * @return the HTTP response
     */
    public static <T, R> ResponseEntity<?> toResponseEntityFromResult(
            Result<PageResult<T>, ApplicationError> result,
            boolean paged,
            Function<T, R> mapper) {
        return result.fold(
                pageResult -> toResponseEntityFromPageResult(pageResult, paged, mapper),
                ProblemDetailFromApplicationErrorAssembler::toResponseFromApplicationError);
    }
}
