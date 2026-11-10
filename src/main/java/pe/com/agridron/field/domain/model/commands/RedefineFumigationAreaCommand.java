package pe.com.agridron.field.domain.model.commands;

import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;

/**
 * Command that redefines the boundary of an existing fumigation area.
 *
 * @param fumigationAreaId identifier of the fumigation area
 * @param geometry         the new validated boundary
 */
public record RedefineFumigationAreaCommand(Long fumigationAreaId, FieldGeometry geometry) {
}
