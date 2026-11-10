package pe.com.agridron.inventory.domain.model.valueobjects;


/**
 * Estado operativo de un dron.
 *
 * <p>En la API se exponen en minúsculas con guiones ({@code available}, {@code in-flight},
 * {@code maintenance}, {@code out-of-service}), como los usa el frontend; esa conversión se
 * hace en la capa interfaces, no aquí.</p>
 */
public enum DroneStatus {
    AVAILABLE,
    IN_FLIGHT,
    MAINTENANCE,
    OUT_OF_SERVICE
}