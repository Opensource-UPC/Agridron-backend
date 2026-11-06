package pe.com.agridron.flight.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.MissionPersistenceEntity;

import java.util.Collection;
import java.util.Optional;

/**
 * Spring Data repository for mission persistence entities.
 */
@Repository
public interface MissionPersistenceRepository
        extends JpaRepository<MissionPersistenceEntity, Long>, JpaSpecificationExecutor<MissionPersistenceEntity> {

    /**
     * Loads a mission and forces its version to increase on commit, so that changes made only
     * to child entities (incidents, telemetry, result) still detect concurrent updates.
     */
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<MissionPersistenceEntity> findForUpdateById(Long id);

    boolean existsByParcelIdAndStatusIn(ParcelId parcelId, Collection<MissionStatus> statuses);
}
