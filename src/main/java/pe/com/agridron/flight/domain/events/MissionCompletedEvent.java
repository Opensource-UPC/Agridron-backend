package pe.com.agridron.flight.domain.events;

import pe.com.agridron.flight.domain.model.aggregates.Mission;

/**
 * Raised when a mission is completed and its result has been registered.
 */
public record MissionCompletedEvent(
        Long missionId,
        Long parcelId,
        Long droneId,
        Long agrochemicalId,
        Long operatorId,
        Double treatedArea,
        Double appliedVolume) {

    public static MissionCompletedEvent from(Mission mission) {
        return new MissionCompletedEvent(
                mission.getId(),
                mission.getParcelId().parcelId(),
                mission.getDroneId().droneId(),
                mission.getAgrochemicalId().agrochemicalId(),
                mission.getOperatorId().operatorId(),
                mission.getResult().getTreatedArea(),
                mission.getResult().getAppliedVolume());
    }
}
