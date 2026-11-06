package pe.com.agridron.flight.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

import java.time.Instant;

/**
 * JPA representation of an incident reported during a mission.
 *
 * <p>Incidents are append-only and their concurrency is guarded by the version of the
 * owning mission, so the non-auditable base entity is enough.</p>
 */
@Entity
@Table(name = "incident")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentPersistenceEntity extends AbstractPersistenceEntity {

    @Setter(AccessLevel.PACKAGE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false, updatable = false)
    private MissionPersistenceEntity mission;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public IncidentPersistenceEntity(String type, String description, Instant occurredAt) {
        this.type = type;
        this.description = description;
        this.occurredAt = occurredAt;
    }
}
