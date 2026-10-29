package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;

/**
 * Spring Data repository for crop persistence entities.
 */
@Repository
public interface CropPersistenceRepository extends JpaRepository<CropPersistenceEntity, Long> {
}
