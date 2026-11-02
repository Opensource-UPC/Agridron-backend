package pe.com.agridron.field.domain.model.queries;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationTest {

    @Test
    void createsAPaginatedRequest() {
        var pagination = Pagination.of(2, 25);

        assertThat(pagination.page()).isEqualTo(2);
        assertThat(pagination.size()).isEqualTo(25);
        assertThat(pagination.paged()).isTrue();
    }

    @Test
    void createsAnUnpagedRequest() {
        var pagination = Pagination.unpaged();

        assertThat(pagination.page()).isZero();
        assertThat(pagination.size()).isZero();
        assertThat(pagination.paged()).isFalse();
    }

    @Test
    void rejectsNegativePage() {
        assertThatThrownBy(() -> Pagination.of(-1, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void rejectsSizeOutOfRange() {
        assertThatThrownBy(() -> Pagination.of(0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> Pagination.of(0, Pagination.MAX_SIZE + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    @Test
    void rejectsAnUnpagedRequestWithData() {
        assertThatThrownBy(() -> new Pagination(1, 10, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unpaged");
    }
}
