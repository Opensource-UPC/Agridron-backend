package pe.com.agridron.inventory.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * JSON de salida de un dron. Tiene los mismos campos que usa el frontend.
 */
@Schema(name = "DroneResponse", description = "Drone information")
public record DroneResource(
        @Schema(description = "Drone identifier", example = "1")
        Long id,

        @Schema(description = "Serial number", example = "DRN-2026-0101")
        String serialNumber,

        @Schema(description = "Drone model", example = "RTX-6030")
        String model,

        @Schema(description = "Battery level (0-100)", example = "80")
        int batteryLevel,

        @Schema(description = "Operational status",
                allowableValues = {"available", "in-flight", "maintenance", "out-of-service"},
                example = "available")
        String status,

        @Schema(description = "Installed nozzle identifier", example = "1")
        Long nozzleId,

        @Schema(description = "Tank capacity in liters", example = "35")
        Double tankCapacityLiters,

        @Schema(description = "Last maintenance date (yyyy-MM-dd) or null", example = "2026-11-04")
        String lastMaintenanceDate,

        @Schema(description = "Next maintenance date (yyyy-MM-dd) or null", example = "2027-01-05")
        String nextMaintenanceDate,

        @Schema(description = "Image URL or null")
        String urlimg,

        @Schema(description = "Accumulated flight hours", example = "0")
        int flightHours) {
}