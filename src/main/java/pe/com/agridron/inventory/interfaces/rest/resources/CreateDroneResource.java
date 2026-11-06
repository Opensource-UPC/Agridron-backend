package pe.com.agridron.inventory.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * JSON de entrada para registrar un dron.
 *
 * <p>Ignora campos que el frontend envía pero que el servidor decide solo
 * ({@code id}, {@code status}, {@code flightHours}): un dron nuevo siempre queda disponible
 * y con 0 horas. Las fechas llegan como texto {@code yyyy-MM-dd} y pueden venir vacías.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "CreateDroneRequest", description = "Data required to register a drone")
public record CreateDroneResource(
        @Schema(description = "Serial number, unique in the fleet", example = "DRN-2026-0101")
        @NotBlank(message = "is required")
        @Size(max = 40, message = "must have at most 40 characters")
        String serialNumber,

        @Schema(description = "Drone model", example = "RTX-6030")
        @NotBlank(message = "is required")
        @Size(max = 80, message = "must have at most 80 characters")
        String model,

        @Schema(description = "Battery level (0-100)", example = "80")
        @NotNull(message = "is required")
        @Min(value = 0, message = "must be between 0 and 100")
        @Max(value = 100, message = "must be between 0 and 100")
        Integer batteryLevel,

        @Schema(description = "Initial nozzle installed in the drone", example = "1")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Long nozzleId,

        @Schema(description = "Tank capacity in liters", example = "35")
        @NotNull(message = "is required")
        @Positive(message = "must be a positive number")
        Double tankCapacityLiters,

        @Schema(description = "Last maintenance date (yyyy-MM-dd), optional", example = "2026-11-04")
        String lastMaintenanceDate,

        @Schema(description = "Next maintenance date (yyyy-MM-dd), optional", example = "2027-01-05")
        String nextMaintenanceDate,

        @Schema(description = "Image URL, optional", example = "https://images.unsplash.com/photo-1521405924368-64c5b84bec60?w=600")
        @Size(max = 500, message = "must have at most 500 characters")
        String urlimg) {
}