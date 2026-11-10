package pe.com.agridron.field.domain.model.queries;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParcelSearchCriteriaTest {

    @Test
    void acceptsEmptyCriteria() {
        assertThatCode(() -> new ParcelSearchCriteria(null, null, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void acceptsValidCriteria() {
        var criteria = new ParcelSearchCriteria(
                1L,
                ParcelStatus.ACTIVE,
                2L,
                new BigDecimal("1.0"),
                new BigDecimal("5.0"));

        assertThat(criteria.farmId()).isEqualTo(1L);
        assertThat(criteria.status()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(criteria.cropId()).isEqualTo(2L);
        assertThat(criteria.pagination()).isEqualTo(Pagination.unpaged());
        assertThat(criteria.sortOrder()).isNull();
    }

    @Test
    void keepsPaginationAndOrdering() {
        var criteria = new ParcelSearchCriteria(
                null,
                null,
                null,
                null,
                null,
                Pagination.of(1, 10),
                new SortOrder("areaHectares", SortDirection.DESC));

        assertThat(criteria.pagination().page()).isEqualTo(1);
        assertThat(criteria.pagination().size()).isEqualTo(10);
        assertThat(criteria.sortOrder().field()).isEqualTo("areaHectares");
        assertThat(criteria.sortOrder().direction()).isEqualTo(SortDirection.DESC);
    }

    @Test
    void rejectsNullPagination() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(
                null, null, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pagination");
    }

    @Test
    void rejectsInvalidFarmId() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(0L, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId");
    }

    @Test
    void rejectsInvalidCropId() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(null, null, -1L, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cropId");
    }

    @Test
    void rejectsNegativeMinimumArea() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(
                null, null, null, new BigDecimal("-0.1"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minAreaHectares");
    }

    @Test
    void rejectsNegativeMaximumArea() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(
                null, null, null, null, new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxAreaHectares");
    }

    @Test
    void rejectsMinimumAreaGreaterThanMaximumArea() {
        assertThatThrownBy(() -> new ParcelSearchCriteria(
                null, null, null, new BigDecimal("5.0"), new BigDecimal("1.0")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minAreaHectares");
    }
}
