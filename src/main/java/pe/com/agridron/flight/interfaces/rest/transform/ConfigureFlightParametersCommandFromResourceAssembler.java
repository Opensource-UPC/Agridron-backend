package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.commands.ConfigureFlightParametersCommand;
import pe.com.agridron.flight.interfaces.rest.resources.FlightParametersResource;

/**
 * Assembler that converts a {@link FlightParametersResource} into a {@link ConfigureFlightParametersCommand}.
 */
public final class ConfigureFlightParametersCommandFromResourceAssembler {

    private ConfigureFlightParametersCommandFromResourceAssembler() {
    }

    public static ConfigureFlightParametersCommand toCommandFromResource(
            Long missionId, FlightParametersResource resource) {
        return new ConfigureFlightParametersCommand(
                missionId,
                resource.flightHeight(),
                resource.flightSpeed(),
                resource.sprayRate(),
                resource.overlap());
    }
}
