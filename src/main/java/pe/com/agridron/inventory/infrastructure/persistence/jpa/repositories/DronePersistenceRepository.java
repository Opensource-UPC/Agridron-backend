package pe.com.agridron.inventory.infrastructure.persistence.jpa.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.DronePersistenceEntity;

/**
 * Repositorio Spring Data de la tabla {@code drone}.
 */
@Repository
public interface DronePersistenceRepository extends JpaRepository<DronePersistenceEntity, Long> {

    boolean existsBySerialNumber(String serialNumber);
}