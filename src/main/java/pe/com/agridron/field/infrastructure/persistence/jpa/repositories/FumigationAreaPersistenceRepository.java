package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    List<FumigationAreaPersistenceEntity> findAllByParcelId(Long parcelId, Sort sort);

    Page<FumigationAreaPersistenceEntity> findAllByParcelId(Long parcelId, Pageable pageable);
}
