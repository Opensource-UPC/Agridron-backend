package pe.com.agridron.field.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;

/**
 * Request body for updating the editable data of a parcel.
 *
 * <p>The boundary is changed through the dedicated geometry endpoint.</p>
 *
 * @param name              new parcel name, unique inside its farm
 * @param cropId            optional crop from the read-only catalog
 * @param phenologicalStage required when {@code cropId} is present
 */
@Schema(name = "UpdateParcelRequest", description = "Data required to update a parcel")
public record UpdateParcelResource(
        @Schema(description = "Parcel name", example = "Lote 1")
        @NotBlank(message = "is required")
        @Size(max = 120, message = "must have at most 120 characters")
        String name,

        @Schema(description = "Optional crop identifier", example = "2")
        @Positive(message = "must be a positive number")
        Long cropId,

        @Schema(description = "Phenological stage; required when cropId is present", example = "FLOWERING")
        PhenologicalStage phenologicalStage) {
}
