package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.interfaces.rest.resources.ParcelResource;

/**
 * Assembler that converts a {@link Parcel} aggregate into a {@link ParcelResource}.
 */
public final class ParcelResourceFromEntityAssembler {

    private ParcelResourceFromEntityAssembler() {
    }

    /**
     * Converts the aggregate into its REST representation, including the GeoJSON boundary
     * and the area calculated by the domain.
     *
     * @param parcel the parcel aggregate
     * @return the response resource
     */
    public static ParcelResource toResourceFromEntity(Parcel parcel) {
        return new ParcelResource(
                parcel.getId(),
                parcel.getFarmId(),
                parcel.getCropId(),
                parcel.getName(),
                PolygonResourceFromFieldGeometryAssembler
                        .toResourceFromFieldGeometry(parcel.getGeometry()),
                parcel.getGeometry().areaHectares(),
                parcel.getStatus(),
                parcel.getPhenologicalStage());
    }
}
