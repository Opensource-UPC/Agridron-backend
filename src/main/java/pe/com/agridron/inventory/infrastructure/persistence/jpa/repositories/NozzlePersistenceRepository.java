package pe.com.agridron.inventory.infrastructure.persistence.jpa.repositories;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.NozzlePersistenceEntity;

/**
 * Repositorio Spring Data de la tabla {@code nozzle}.
 */
@Repository
public interface NozzlePersistenceRepository extends JpaRepository<NozzlePersistenceEntity, Long> {
}