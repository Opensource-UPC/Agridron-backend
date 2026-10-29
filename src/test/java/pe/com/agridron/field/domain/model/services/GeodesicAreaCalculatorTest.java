package pe.com.agridron.field.domain.model.services;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class GeodesicAreaCalculatorTest {

    @Test
    void computesTheAreaOfAOneDegreeSquareAtTheEquator() {
        var square = TestGeometries.wgs84Square(-0.5, -0.5, 1.0);

        var hectares = GeodesicAreaCalculator.areaInHectares(square);

        assertThat(hectares)
                .isGreaterThan(new BigDecimal("1200000"))
                .isLessThan(new BigDecimal("1280000"));
    }

    @Test
    void computesSmallParcelAreaInHectares() {
        var parcel = TestGeometries.wgs84Square(-77.042, -12.046, 0.001);

        var hectares = GeodesicAreaCalculator.areaInHectares(parcel);

        assertThat(hectares)
                .isGreaterThan(new BigDecimal("1.0"))
                .isLessThan(new BigDecimal("1.4"));
    }

    @Test
    void returnsAreaWithFourDecimals() {
        var hectares = GeodesicAreaCalculator.areaInHectares(TestGeometries.wgs84Square(0, 0, 0.01));

        assertThat(hectares.scale()).isEqualTo(4);
        assertThat(hectares).isPositive();
    }

    @Test
    void subtractsHolesFromTheGeodesicArea() {
        var withHole = GeodesicAreaCalculator.areaInHectares(TestGeometries.wgs84PolygonWithHole());
        var withoutHole = GeodesicAreaCalculator.areaInHectares(TestGeometries.wgs84Square(0, 0, 0.01));

        assertThat(withHole).isLessThan(withoutHole);
    }

    @Test
    void computesAreaInSquareMeters() {
        var squareMeters = GeodesicAreaCalculator.areaInSquareMeters(
                TestGeometries.wgs84Square(-77.042, -12.046, 0.001));

        assertThat(squareMeters).isPositive();
    }
}
