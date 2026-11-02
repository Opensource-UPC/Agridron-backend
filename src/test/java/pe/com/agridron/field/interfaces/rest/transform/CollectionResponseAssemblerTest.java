package pe.com.agridron.field.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.interfaces.rest.resources.PageResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionResponseAssemblerTest {

    @Test
    void keepsTheArrayContractWhenTheRequestIsUnpaged() {
        var page = PageResult.unpaged(List.of("a", "b"));

        var response = CollectionResponseAssembler.toResponseEntityFromPageResult(
                page, false, String::toUpperCase);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(List.of("A", "B"));
    }

    @Test
    void returnsAnEnvelopeWhenTheRequestIsPaged() {
        var page = new PageResult<>(List.of("a", "b"), 1, 2, 5);

        var response = CollectionResponseAssembler.toResponseEntityFromPageResult(
                page, true, String::toUpperCase);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOfSatisfying(PageResource.class, resource -> {
            assertThat(resource.content()).containsExactly("A", "B");
            assertThat(resource.page()).isEqualTo(1);
            assertThat(resource.size()).isEqualTo(2);
            assertThat(resource.totalElements()).isEqualTo(5);
            assertThat(resource.totalPages()).isEqualTo(3);
            assertThat(resource.first()).isFalse();
            assertThat(resource.last()).isFalse();
        });
    }
}
