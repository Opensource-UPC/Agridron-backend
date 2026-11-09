package pe.com.agridron.inventory.interfaces.rest.transform;


import pe.com.agridron.inventory.domain.model.commands.CreateNozzleCommand;
import pe.com.agridron.inventory.interfaces.rest.resources.CreateNozzleResource;

/**
 * Convierte el JSON de entrada en un {@link CreateNozzleCommand}.
 */
public final class CreateNozzleCommandFromResourceAssembler {

    private CreateNozzleCommandFromResourceAssembler() {
    }

    public static CreateNozzleCommand toCommandFromResource(CreateNozzleResource resource) {
        return new CreateNozzleCommand(
                resource.type(),
                resource.diameterMm(),
                resource.goodUnits(),
                resource.wornUnits(),
                resource.damagedUnits());
    }
}