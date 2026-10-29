package pe.com.agridron.field.interfaces.rest.transform;

import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.interfaces.rest.resources.FarmResource;

import java.util.List;

/**
 * Assembler that converts a {@link Farm} aggregate into a {@link FarmResource}.
 */
public final class FarmResourceFromEntityAssembler {

    private FarmResourceFromEntityAssembler() {
    }

    /**
     * Converts the aggregate into its REST representation. Persistence details are never exposed.
     *
     * @param farm the farm aggregate
     * @return the response resource
     */
    public static FarmResource toResourceFromEntity(Farm farm) {
        return new FarmResource(
                farm.getId(),
                farm.getOwnerId(),
                farm.getName(),
                farm.getLocation(),
                farm.getDepartment(),
                List.copyOf(farm.getSupervisorIds()));
    }
}
