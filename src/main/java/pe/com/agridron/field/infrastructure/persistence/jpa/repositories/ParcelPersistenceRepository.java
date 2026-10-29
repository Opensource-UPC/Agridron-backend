package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;

import java.util.List;

/**
 * Spring Data repository for parcel persistence entities.
 */
@Repository
public interface ParcelPersistenceRepository
        extends JpaRepository<ParcelPersistenceEntity, Long>,
        JpaSpecificationExecutor<ParcelPersistenceEntity> {

    List<ParcelPersistenceEntity> findAllByFarmIdOrderByNameAsc(Long farmId);

    boolean existsByFarmIdAndName(Long farmId, String name);

    boolean existsByFarmIdAndNameAndIdNot(Long farmId, String name, Long id);
}
