package pe.com.agridron.field.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.model.queries.SortDirection;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SortOrderFromRequestAssemblerTest {

    private static final Set<String> ALLOWED = Set.of("id", "name", "location");

    @Test
    void returnsNullWhenSortIsAbsentOrBlank() {
        assertThat(SortOrderFromRequestAssembler.toSortOrderFromRequest(null, ALLOWED)).isNull();
        assertThat(SortOrderFromRequestAssembler.toSortOrderFromRequest("  ", ALLOWED)).isNull();
    }

    @Test
    void defaultsToAscending() {
        var order = SortOrderFromRequestAssembler.toSortOrderFromRequest("name", ALLOWED);

        assertThat(order.field()).isEqualTo("name");
        assertThat(order.direction()).isEqualTo(SortDirection.ASC);
    }

    @Test
    void parsesFieldAndDirectionIgnoringCase() {
        var order = SortOrderFromRequestAssembler.toSortOrderFromRequest("  NAME , DESC ", ALLOWED);

        assertThat(order.field()).isEqualTo("name");
        assertThat(order.direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void rejectsUnknownField() {
        assertThatThrownBy(() ->
                SortOrderFromRequestAssembler.toSortOrderFromRequest("ownerId", ALLOWED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not supported");
    }

    @Test
    void rejectsInvalidDirection() {
        assertThatThrownBy(() ->
                SortOrderFromRequestAssembler.toSortOrderFromRequest("name,sideways", ALLOWED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("direction");
    }

    @Test
    void rejectsTooManyParts() {
        assertThatThrownBy(() ->
                SortOrderFromRequestAssembler.toSortOrderFromRequest("name,asc,id", ALLOWED))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("format");
    }
}
