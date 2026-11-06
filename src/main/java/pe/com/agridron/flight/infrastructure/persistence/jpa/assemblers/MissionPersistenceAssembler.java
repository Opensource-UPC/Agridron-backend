package pe.com.agridron.flight.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.domain.model.entities.MissionResult;
import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.flight.domain.model.valueobjects.FlightCoordinates;
import pe.com.agridron.flight.domain.model.valueobjects.FlightParameters;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.IncidentPersistenceEntity;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.MissionPersistenceEntity;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.MissionResultPersistenceEntity;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.OperationStatusPersistenceEntity;

/**
 * Static assembler between the mission aggregate and its JPA representation.
 */
public final class MissionPersistenceAssembler {

    private MissionPersistenceAssembler() {
    }

    /**
     * Rebuilds the mission aggregate, including its child entities, from its persisted state.
     */
    public static Mission toDomainFromPersistence(MissionPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Mission.reconstitute(
                entity.getId(),
                entity.getRequestedDate(),
                entity.getParcelId(),
                entity.getAgrochemicalId(),
                entity.getDroneId(),
                entity.getOperatorId(),
                toFlightParameters(entity),
                entity.getStatus(),
                toResult(entity.getResult()),
                toOperationStatus(entity.getOperationStatus()),
                entity.getIncidents().stream().map(MissionPersistenceAssembler::toIncident).toList());
    }

    /**
     * Builds a new persistence entity from a mission aggregate that has not been saved yet.
     */
    public static MissionPersistenceEntity toPersistenceFromDomain(Mission mission) {
        if (mission == null) {
            return null;
        }
        var entity = new MissionPersistenceEntity(
                mission.getParcelId(), mission.getRequestedDate(), mission.getStatus());
        copyToPersistence(mission, entity);
        return entity;
    }

    /**
     * Copies the mutable state of the aggregate into an existing entity, preserving its
     * identity and optimistic-locking version. New child entities (those without id) are
     * attached; persisted incidents are immutable and are never rewritten.
     */
    public static void copyToPersistence(Mission mission, MissionPersistenceEntity entity) {
        entity.setRequestedDate(mission.getRequestedDate());
        entity.setAgrochemicalId(mission.getAgrochemicalId());
        entity.setDroneId(mission.getDroneId());
        entity.setOperatorId(mission.getOperatorId());
        entity.setStatus(mission.getStatus());

        var parameters = mission.getFlightParameters();
        entity.setFlightHeight(parameters == null ? null : parameters.flightHeight());
        entity.setFlightSpeed(parameters == null ? null : parameters.flightSpeed());
        entity.setSprayRate(parameters == null ? null : parameters.sprayRate());
        entity.setOverlap(parameters == null ? null : parameters.overlap());

        var result = mission.getResult();
        if (result != null && entity.getResult() == null) {
            entity.attachResult(new MissionResultPersistenceEntity(
                    result.getTreatedArea(), result.getAppliedVolume(), result.getObservations()));
        }

        var status = mission.getOperationStatus();
        if (status != null) {
            var coordinates = status.getFlightCoordinates();
            if (entity.getOperationStatus() == null) {
                entity.attachOperationStatus(new OperationStatusPersistenceEntity(
                        coordinates.latitude(), coordinates.longitude(), status.getStatus(), status.getTimestamp()));
            } else {
                entity.getOperationStatus().update(
                        coordinates.latitude(), coordinates.longitude(), status.getStatus(), status.getTimestamp());
            }
        }

        mission.getIncidents().stream()
                .filter(incident -> incident.getId() == null)
                .forEach(incident -> entity.addIncident(new IncidentPersistenceEntity(
                        incident.getType(), incident.getDescription(), incident.getOccurredAt())));
    }

    private static FlightParameters toFlightParameters(MissionPersistenceEntity entity) {
        if (entity.getFlightHeight() == null) {
            return null;
        }
        return new FlightParameters(
                entity.getFlightHeight(), entity.getFlightSpeed(), entity.getSprayRate(), entity.getOverlap());
    }

    private static MissionResult toResult(MissionResultPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return MissionResult.reconstitute(
                entity.getId(), entity.getTreatedArea(), entity.getAppliedVolume(), entity.getObservations());
    }

    private static OperationStatus toOperationStatus(OperationStatusPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return OperationStatus.reconstitute(
                entity.getId(),
                new FlightCoordinates(entity.getLatitude(), entity.getLongitude()),
                entity.getState(),
                entity.getRecordedAt());
    }

    private static Incident toIncident(IncidentPersistenceEntity entity) {
        return Incident.reconstitute(
                entity.getId(), entity.getType(), entity.getDescription(), entity.getOccurredAt());
    }
}
