package pe.com.agridron.flight.domain.model.aggregates;

import lombok.Getter;
import pe.com.agridron.flight.domain.events.IncidentReportedEvent;
import pe.com.agridron.flight.domain.events.MissionCompletedEvent;
import pe.com.agridron.flight.domain.events.MissionPlannedEvent;
import pe.com.agridron.flight.domain.events.MissionStatusChangedEvent;
import pe.com.agridron.flight.domain.exceptions.InvalidMissionStateException;
import pe.com.agridron.flight.domain.model.entities.Incident;
import pe.com.agridron.flight.domain.model.entities.MissionResult;
import pe.com.agridron.flight.domain.model.entities.OperationStatus;
import pe.com.agridron.flight.domain.model.valueobjects.AgrochemicalId;
import pe.com.agridron.flight.domain.model.valueobjects.DroneId;
import pe.com.agridron.flight.domain.model.valueobjects.FlightCoordinates;
import pe.com.agridron.flight.domain.model.valueobjects.FlightParameters;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.model.valueobjects.OperationState;
import pe.com.agridron.flight.domain.model.valueobjects.OperatorId;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.shared.domain.model.aggregates.AbstractDomainAggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Mission aggregate root.
 *
 * <p>A mission is a spraying flight over a parcel. It owns its lifecycle, the assigned
 * resources (drone, agrochemical and operator), the flight parameters and, as child
 * entities, the incidents, the last operational status and the final result. Parcels,
 * drones, agrochemicals and operators belong to other bounded contexts and are
 * referenced by id only.</p>
 */
@Getter
public class Mission extends AbstractDomainAggregateRoot<Mission> {

    private static final Set<MissionStatus> IN_FLIGHT = EnumSet.of(MissionStatus.IN_PROGRESS, MissionStatus.PAUSED);

    private Long id;
    private LocalDate requestedDate;
    private final ParcelId parcelId;
    private AgrochemicalId agrochemicalId;
    private DroneId droneId;
    private OperatorId operatorId;
    private FlightParameters flightParameters;
    private MissionStatus status;
    private MissionResult result;
    private OperationStatus operationStatus;
    private final List<Incident> incidents = new ArrayList<>();

    private Mission(ParcelId parcelId, LocalDate requestedDate) {
        this.parcelId = Objects.requireNonNull(parcelId, "parcelId is required");
        this.requestedDate = Objects.requireNonNull(requestedDate, "requestedDate is required");
        this.status = MissionStatus.PLANNED;
    }

    /**
     * Plans a new mission over a parcel. The {@code MissionPlannedEvent} is registered by
     * {@link #onCreated()} once the persistence adapter has assigned the identity.
     */
    public static Mission plan(ParcelId parcelId, LocalDate requestedDate) {
        return new Mission(parcelId, requestedDate);
    }

    /**
     * Reconstitutes an existing mission from persistence state.
     */
    public static Mission reconstitute(
            Long id,
            LocalDate requestedDate,
            ParcelId parcelId,
            AgrochemicalId agrochemicalId,
            DroneId droneId,
            OperatorId operatorId,
            FlightParameters flightParameters,
            MissionStatus status,
            MissionResult result,
            OperationStatus operationStatus,
            Collection<Incident> incidents) {
        var mission = new Mission(parcelId, requestedDate);
        if (id == null || id < 1) {
            throw new IllegalArgumentException("id must be positive");
        }
        mission.id = id;
        mission.agrochemicalId = agrochemicalId;
        mission.droneId = droneId;
        mission.operatorId = operatorId;
        mission.flightParameters = flightParameters;
        mission.status = Objects.requireNonNull(status, "status is required");
        mission.result = result;
        mission.operationStatus = operationStatus;
        if (incidents != null) {
            mission.incidents.addAll(incidents);
        }
        return mission;
    }

    /**
     * Registers the creation event. Called by the repository adapter after the identity is assigned.
     */
    public void onCreated() {
        registerDomainEvent(MissionPlannedEvent.from(this));
    }

    // ---------------------------------------------------------------- planning

    /**
     * Assigns the drone and the agrochemical that will be used. Only allowed while planned.
     */
    public void assignResources(DroneId droneId, AgrochemicalId agrochemicalId) {
        requireStatus("assign resources", MissionStatus.PLANNED);
        this.droneId = Objects.requireNonNull(droneId, "droneId is required");
        this.agrochemicalId = Objects.requireNonNull(agrochemicalId, "agrochemicalId is required");
    }

    /**
     * Assigns the drone operator. Only allowed while planned.
     */
    public void assignOperator(OperatorId operatorId) {
        requireStatus("assign an operator", MissionStatus.PLANNED);
        this.operatorId = Objects.requireNonNull(operatorId, "operatorId is required");
    }

    /**
     * Configures the flight parameters. Only allowed while planned.
     */
    public void configureFlightParameters(FlightParameters flightParameters) {
        requireStatus("configure flight parameters", MissionStatus.PLANNED);
        this.flightParameters = Objects.requireNonNull(flightParameters, "flightParameters is required");
    }

    /**
     * Moves the mission to another date. Allowed while planned or authorized.
     */
    public void reschedule(LocalDate requestedDate) {
        requireStatus("reschedule", MissionStatus.PLANNED, MissionStatus.AUTHORIZED);
        this.requestedDate = Objects.requireNonNull(requestedDate, "requestedDate is required");
    }

    // ---------------------------------------------------------------- lifecycle

    /**
     * Authorizes a planned mission. Every resource, the operator and the flight
     * parameters must be assigned.
     */
    public void authorize() {
        requireStatus("authorize", MissionStatus.PLANNED);
        var missing = new ArrayList<String>();
        if (droneId == null) missing.add("droneId");
        if (agrochemicalId == null) missing.add("agrochemicalId");
        if (operatorId == null) missing.add("operatorId");
        if (flightParameters == null) missing.add("flightParameters");
        if (!missing.isEmpty()) {
            throw new InvalidMissionStateException(
                    "Mission cannot be authorized without " + String.join(", ", missing));
        }
        changeStatus(MissionStatus.AUTHORIZED);
    }

    /**
     * Starts an authorized mission.
     */
    public void start() {
        requireStatus("start", MissionStatus.AUTHORIZED);
        changeStatus(MissionStatus.IN_PROGRESS);
    }

    /**
     * Pauses a mission in progress.
     */
    public void pause() {
        requireStatus("pause", MissionStatus.IN_PROGRESS);
        changeStatus(MissionStatus.PAUSED);
    }

    /**
     * Resumes a paused mission.
     */
    public void resume() {
        requireStatus("resume", MissionStatus.PAUSED);
        changeStatus(MissionStatus.IN_PROGRESS);
    }

    /**
     * Cancels a mission that has not finished yet.
     */
    public void cancel() {
        if (status.isTerminal()) {
            throw new InvalidMissionStateException("Mission %s cannot be cancelled".formatted(status));
        }
        changeStatus(MissionStatus.CANCELLED);
    }

    /**
     * Completes a mission in progress and registers its result.
     */
    public void complete(Double treatedArea, Double appliedVolume, String observations) {
        requireStatus("complete", MissionStatus.IN_PROGRESS);
        this.result = MissionResult.register(treatedArea, appliedVolume, observations);
        changeStatus(MissionStatus.COMPLETED);
        registerDomainEvent(MissionCompletedEvent.from(this));
    }

    // ---------------------------------------------------------------- execution

    /**
     * Reports an incident. Allowed while the mission is in progress or paused.
     *
     * @return the new incident; its id is assigned when the mission is saved
     */
    public Incident reportIncident(String type, String description, Instant occurredAt) {
        requireInFlight("report an incident");
        var incident = Incident.register(type, description, occurredAt);
        incidents.add(incident);
        registerDomainEvent(IncidentReportedEvent.from(this, incident));
        return incident;
    }

    /**
     * Records a telemetry report. The first report registers the operational status;
     * later ones update it. Reports older than the current status are ignored.
     */
    public void updateOperationStatus(FlightCoordinates coordinates, OperationState state, Instant timestamp) {
        requireInFlight("update the operation status");
        if (operationStatus == null) {
            operationStatus = OperationStatus.register(coordinates, state, timestamp);
            return;
        }
        if (operationStatus.updateLocation(coordinates, timestamp)) {
            operationStatus.updateStatus(state, timestamp);
        }
    }

    /**
     * Returns an unmodifiable view of the incidents, in reporting order.
     */
    public List<Incident> getIncidents() {
        return Collections.unmodifiableList(incidents);
    }

    /**
     * Returns the most recently reported incident, if any.
     */
    public Optional<Incident> latestIncident() {
        return incidents.isEmpty() ? Optional.empty() : Optional.of(incidents.getLast());
    }

    /**
     * Returns {@code true} while the mission still holds its parcel and resources.
     */
    public boolean isActive() {
        return status.isActive();
    }

    private void changeStatus(MissionStatus newStatus) {
        var previous = status;
        status = newStatus;
        registerDomainEvent(new MissionStatusChangedEvent(id, parcelId.parcelId(), previous, newStatus));
    }

    private void requireInFlight(String action) {
        if (!IN_FLIGHT.contains(status)) {
            throw invalidState(action);
        }
    }

    private void requireStatus(String action, MissionStatus... allowed) {
        for (var candidate : allowed) {
            if (status == candidate) {
                return;
            }
        }
        throw invalidState(action);
    }

    private InvalidMissionStateException invalidState(String action) {
        return new InvalidMissionStateException(
                "Cannot %s a mission in status %s".formatted(action, status));
    }
}
