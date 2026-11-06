package pe.com.agridron.flight.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.agridron.flight.domain.model.valueobjects.OperationState;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;

/**
 * JPA representation of the last operational status of a mission.
 *
 * <p>Its concurrency is guarded by the version of the owning mission, so the
 * non-auditable base entity is enough.</p>
 */
@Entity
@Table(name = "operation_status")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationStatusPersistenceEntity extends AbstractPersistenceEntity {

    @Setter(AccessLevel.PACKAGE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false, updatable = false)
    private MissionPersistenceEntity mission;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationState state;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    public OperationStatusPersistenceEntity(
            Double latitude, Double longitude, OperationState state, Instant recordedAt) {
        update(latitude, longitude, state, recordedAt);
    }

    public void update(Double latitude, Double longitude, OperationState state, Instant recordedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.state = state;
        this.recordedAt = recordedAt;
    }
}
