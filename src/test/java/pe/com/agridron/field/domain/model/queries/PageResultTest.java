package pe.com.agridron.field.domain.model.queries;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResultTest {

    @Test
    void wrapsEveryElementWhenUnpaged() {
        var page = PageResult.unpaged(List.of("a", "b", "c"));

        assertThat(page.content()).containsExactly("a", "b", "c");
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(3);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(1);
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void reportsAnEmptyUnpagedResult() {
        var page = PageResult.unpaged(List.of());

        assertThat(page.content()).isEmpty();
        assertThat(page.totalPages()).isZero();
        assertThat(page.last()).isTrue();
    }

    @Test
    void computesNavigationMetadata() {
        var page = new PageResult<>(List.of("e", "f"), 1, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isFalse();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void marksTheLastPage() {
        var page = new PageResult<>(List.of("e"), 2, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void copiesTheContentDefensively() {
        var content = new ArrayList<>(List.of("a"));

        var page = new PageResult<>(content, 0, 20, 1);
        content.add("b");

        assertThat(page.content()).containsExactly("a");
    }

    @Test
    void rejectsInvalidMetadata() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), -1, 20, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0, -1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0, 20, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalElements");
    }
}
