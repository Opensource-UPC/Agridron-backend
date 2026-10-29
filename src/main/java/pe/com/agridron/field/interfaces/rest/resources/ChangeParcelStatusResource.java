package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;

/**
 * Request body for changing the operational status of a parcel.
 *
 * @param status the target status
 */
@Schema(name = "ChangeParcelStatusRequest", description = "New operational status of a parcel")
public record ChangeParcelStatusResource(
        @Schema(description = "Target status", example = "INACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "is required")
        ParcelStatus status) {
}
