package pe.com.agridron.flight.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AbstractPersistenceEntity;

/**
 * JPA representation of a mission result.
 *
 * <p>It extends the non-auditable base entity because it is written once and its
 * concurrency is guarded by the version of the owning mission.</p>
 */
@Entity
@Table(name = "mission_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionResultPersistenceEntity extends AbstractPersistenceEntity {

    @Setter(AccessLevel.PACKAGE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false, updatable = false)
    private MissionPersistenceEntity mission;

    @Column(name = "treated_area", nullable = false)
    private Double treatedArea;

    @Column(name = "applied_volume", nullable = false)
    private Double appliedVolume;

    @Column(length = 1000)
    private String observations;

    public MissionResultPersistenceEntity(Double treatedArea, Double appliedVolume, String observations) {
        this.treatedArea = treatedArea;
        this.appliedVolume = appliedVolume;
        this.observations = observations;
    }
}
