package pe.com.agridron.field.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.model.queries.Pagination;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaginationFromRequestAssemblerTest {

    @Test
    void returnsUnpagedWhenBothParametersAreAbsent() {
        var pagination = PaginationFromRequestAssembler.toPaginationFromRequest(null, null);

        assertThat(pagination.paged()).isFalse();
    }

    @Test
    void usesDefaultSizeWhenOnlyPageIsPresent() {
        var pagination = PaginationFromRequestAssembler.toPaginationFromRequest(2, null);

        assertThat(pagination.paged()).isTrue();
        assertThat(pagination.page()).isEqualTo(2);
        assertThat(pagination.size()).isEqualTo(Pagination.DEFAULT_SIZE);
    }

    @Test
    void usesFirstPageWhenOnlySizeIsPresent() {
        var pagination = PaginationFromRequestAssembler.toPaginationFromRequest(null, 5);

        assertThat(pagination.paged()).isTrue();
        assertThat(pagination.page()).isZero();
        assertThat(pagination.size()).isEqualTo(5);
    }

    @Test
    void createsAPagedRequestWhenBothParametersArePresent() {
        var pagination = PaginationFromRequestAssembler.toPaginationFromRequest(1, 10);

        assertThat(pagination.page()).isEqualTo(1);
        assertThat(pagination.size()).isEqualTo(10);
    }

    @Test
    void rejectsInvalidValues() {
        assertThatThrownBy(() -> PaginationFromRequestAssembler.toPaginationFromRequest(-1, 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PaginationFromRequestAssembler.toPaginationFromRequest(0, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
