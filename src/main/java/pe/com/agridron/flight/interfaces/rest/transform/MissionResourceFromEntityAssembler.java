package pe.com.agridron.flight.interfaces.rest.transform;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.entities.MissionResult;
import pe.com.agridron.flight.domain.model.valueobjects.FlightParameters;
import pe.com.agridron.flight.interfaces.rest.resources.FlightParametersResource;
import pe.com.agridron.flight.interfaces.rest.resources.MissionResource;
import pe.com.agridron.flight.interfaces.rest.resources.MissionResultResource;

/**
 * Assembler that converts a {@link Mission} aggregate into a {@link MissionResource}.
 */
public final class MissionResourceFromEntityAssembler {

    private MissionResourceFromEntityAssembler() {
    }

    /**
     * Converts the aggregate into its REST representation. Persistence details are never exposed.
     */
    public static MissionResource toResourceFromEntity(Mission mission) {
        return new MissionResource(
                mission.getId(),
                mission.getParcelId().parcelId(),
                mission.getRequestedDate(),
                mission.getDroneId() == null ? null : mission.getDroneId().droneId(),
                mission.getAgrochemicalId() == null ? null : mission.getAgrochemicalId().agrochemicalId(),
                mission.getOperatorId() == null ? null : mission.getOperatorId().operatorId(),
                toFlightParametersResource(mission.getFlightParameters()),
                mission.getStatus(),
                toResultResource(mission.getResult()),
                mission.getOperationStatus() == null
                        ? null
                        : OperationStatusResourceFromEntityAssembler.toResourceFromEntity(mission.getOperationStatus()),
                mission.getIncidents().size());
    }

    private static FlightParametersResource toFlightParametersResource(FlightParameters parameters) {
        if (parameters == null) {
            return null;
        }
        return new FlightParametersResource(
                parameters.flightHeight(),
                parameters.flightSpeed(),
                parameters.sprayRate(),
                parameters.overlap());
    }

    private static MissionResultResource toResultResource(MissionResult result) {
        if (result == null) {
            return null;
        }
        return new MissionResultResource(
                result.getId(),
                result.getTreatedArea(),
                result.getAppliedVolume(),
                result.getObservations());
    }
}
