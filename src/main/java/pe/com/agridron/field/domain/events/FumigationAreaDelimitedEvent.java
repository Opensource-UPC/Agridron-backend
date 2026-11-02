package pe.com.agridron.field.domain.events;

import pe.com.agridron.field.domain.model.aggregates.FumigationArea;

import java.math.BigDecimal;

/**
 * Raised when a fumigation area is delimited or its boundary changes.
 */
public record FumigationAreaDelimitedEvent(
        Long fumigationAreaId,
        Long parcelId,
        BigDecimal areaHectares) {

    public static FumigationAreaDelimitedEvent from(FumigationArea area) {
        return new FumigationAreaDelimitedEvent(
                area.getId(),
                area.getParcelId(),
                area.getGeometry().areaHectares());
    }
}
