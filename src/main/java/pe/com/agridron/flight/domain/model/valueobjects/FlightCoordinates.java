package pe.com.agridron.flight.domain.model.valueobjects;

/**
 * WGS 84 position reported by a drone during a mission.
 *
 * @param latitude  latitude in decimal degrees, between -90 and 90
 * @param longitude longitude in decimal degrees, between -180 and 180
 */
public record FlightCoordinates(Double latitude, Double longitude) {

    public FlightCoordinates {
        if (latitude == null || !Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude == null || !Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }

    /**
     * Returns the coordinates as {@code "latitude,longitude"}.
     */
    public String getCoordinates() {
        return "%s,%s".formatted(latitude, longitude);
    }
}
