package pe.com.agridron.inventory.interfaces.rest.transform;

import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.interfaces.rest.resources.NozzleResource;

/**
 * Convierte el agregado {@link Nozzle} en el JSON de salida (el stock va en tres campos).
 */
public final class NozzleResourceFromEntityAssembler {

    private NozzleResourceFromEntityAssembler() {
    }

    public static NozzleResource toResourceFromEntity(Nozzle nozzle) {
        var stock = nozzle.getStock();
        return new NozzleResource(
                nozzle.getId(),
                nozzle.getType(),
                nozzle.getDiameterMm(),
                stock.goodUnits(),
                stock.wornUnits(),
                stock.damagedUnits());
    }
}