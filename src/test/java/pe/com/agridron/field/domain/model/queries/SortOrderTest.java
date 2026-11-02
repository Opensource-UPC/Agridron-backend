package pe.com.agridron.field.domain.model.queries;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortOrderTest {

    @Test
    void trimsTheField() {
        var order = new SortOrder("  name  ", SortDirection.DESC);

        assertThat(order.field()).isEqualTo("name");
        assertThat(order.direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void rejectsBlankField() {
        assertThatThrownBy(() -> new SortOrder("  ", SortDirection.ASC))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("field");
    }

    @Test
    void rejectsNullDirection() {
        assertThatThrownBy(() -> new SortOrder("name", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("direction");
    }
}
