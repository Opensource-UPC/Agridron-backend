package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * GeoJSON representation of a field polygon (RFC 7946) used at the REST boundary.
 *
 * <p>The coordinates are a list of linear rings: the first one is the exterior ring and
 * the remaining ones are holes. Every position is a {@code [longitude, latitude]} pair
 * in WGS 84 (SRID 4326).</p>
 */
@Schema(
        name = "Polygon",
        description = "GeoJSON polygon in WGS 84. The first ring is the exterior boundary; "
                + "additional rings are holes. Every position is [longitude, latitude].",
        example = "{\"type\":\"Polygon\",\"coordinates\":"
                + "[[[-77.042,-12.046],[-77.041,-12.046],[-77.041,-12.045],"
                + "[-77.042,-12.045],[-77.042,-12.046]]]}")
public record PolygonResource(
        @Schema(
                description = "GeoJSON geometry type; it must be Polygon",
                example = "Polygon",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String type,

        @Schema(
                description = "Linear rings of the polygon in longitude/latitude order",
                requiredMode = Schema.RequiredMode.REQUIRED)
        List<List<List<Double>>> coordinates) {
}
