package pe.com.agridron.flight.infrastructure.persistence.jpa.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import pe.com.agridron.flight.domain.model.valueobjects.AgrochemicalId;
import pe.com.agridron.flight.domain.model.valueobjects.DroneId;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.model.valueobjects.OperatorId;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.flight.infrastructure.persistence.jpa.converters.AgrochemicalIdPersistenceConverter;
import pe.com.agridron.flight.infrastructure.persistence.jpa.converters.DroneIdPersistenceConverter;
import pe.com.agridron.flight.infrastructure.persistence.jpa.converters.OperatorIdPersistenceConverter;
import pe.com.agridron.flight.infrastructure.persistence.jpa.converters.ParcelIdPersistenceConverter;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * JPA representation of a mission. Child entities are owned through cascading
 * associations; the optimistic-locking version guards the whole aggregate.
 */
@Entity
@Table(name = "mission")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Convert(converter = ParcelIdPersistenceConverter.class)
    @Column(name = "parcel_id", nullable = false, updatable = false)
    private ParcelId parcelId;

    @Convert(converter = AgrochemicalIdPersistenceConverter.class)
    @Column(name = "agrochemical_id")
    private AgrochemicalId agrochemicalId;

    @Convert(converter = DroneIdPersistenceConverter.class)
    @Column(name = "drone_id")
    private DroneId droneId;

    @Convert(converter = OperatorIdPersistenceConverter.class)
    @Column(name = "operator_id")
    private OperatorId operatorId;

    @Column(name = "flight_height")
    private Double flightHeight;

    @Column(name = "flight_speed")
    private Double flightSpeed;

    @Column(name = "spray_rate")
    private Double sprayRate;

    @Column(name = "overlap")
    private Double overlap;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MissionStatus status;

    @Setter(AccessLevel.NONE)
    @OneToOne(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private MissionResultPersistenceEntity result;

    @Setter(AccessLevel.NONE)
    @OneToOne(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    private OperationStatusPersistenceEntity operationStatus;

    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "mission", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @BatchSize(size = 50)
    private List<IncidentPersistenceEntity> incidents = new ArrayList<>();

    public MissionPersistenceEntity(ParcelId parcelId, LocalDate requestedDate, MissionStatus status) {
        this.parcelId = parcelId;
        this.requestedDate = requestedDate;
        this.status = status;
    }

    /**
     * Attaches the mission result, keeping both sides of the association in sync.
     */
    public void attachResult(MissionResultPersistenceEntity result) {
        result.setMission(this);
        this.result = result;
    }

    /**
     * Attaches the first operational status, keeping both sides of the association in sync.
     */
    public void attachOperationStatus(OperationStatusPersistenceEntity operationStatus) {
        operationStatus.setMission(this);
        this.operationStatus = operationStatus;
    }

    /**
     * Appends an incident, keeping both sides of the association in sync.
     */
    public void addIncident(IncidentPersistenceEntity incident) {
        incident.setMission(this);
        incidents.add(incident);
    }

    /**
     * Returns an unmodifiable view of the incidents, ordered by id.
     */
    public List<IncidentPersistenceEntity> getIncidents() {
        return Collections.unmodifiableList(incidents);
    }
}
