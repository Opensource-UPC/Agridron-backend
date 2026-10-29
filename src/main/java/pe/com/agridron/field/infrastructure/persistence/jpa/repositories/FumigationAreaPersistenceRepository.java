package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FumigationAreaPersistenceEntity;

import java.util.List;

/**
 * Spring Data repository for fumigation area persistence entities.
 */
@Repository
public interface FumigationAreaPersistenceRepository
        extends JpaRepository<FumigationAreaPersistenceEntity, Long> {

    List<FumigationAreaPersistenceEntity> findAllByParcelIdOrderByIdAsc(Long parcelId);
}
