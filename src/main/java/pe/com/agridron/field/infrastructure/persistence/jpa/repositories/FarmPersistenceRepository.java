package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;

import java.util.List;

/**
 * Spring Data repository for farm persistence entities.
 */
@Repository
public interface FarmPersistenceRepository extends JpaRepository<FarmPersistenceEntity, Long> {

    List<FarmPersistenceEntity> findAllByOwnerId(Long ownerId, Sort sort);

    Page<FarmPersistenceEntity> findAllByOwnerId(Long ownerId, Pageable pageable);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long id);
}
