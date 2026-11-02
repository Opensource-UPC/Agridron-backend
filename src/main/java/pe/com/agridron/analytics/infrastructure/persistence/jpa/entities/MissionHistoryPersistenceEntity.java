package pe.com.agridron.analytics.infrastructure.persistence.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.Instant;

@Entity
@Table(name = "mission_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionHistoryPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "mission_id", nullable = false)
    private Long missionId;

    @Column(name = "parcel_id", nullable = false)
    private Long parcelId;

    @Enumerated(EnumType.STRING)
    @Column(name = "final_status", nullable = false, length = 20)
    private MissionFinalStatus finalStatus;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    public MissionHistoryPersistenceEntity(
            Long missionId, Long parcelId, MissionFinalStatus finalStatus, Instant completedAt) {
        this.missionId = missionId;
        this.parcelId = parcelId;
        this.finalStatus = finalStatus;
        this.completedAt = completedAt;
    }
}
