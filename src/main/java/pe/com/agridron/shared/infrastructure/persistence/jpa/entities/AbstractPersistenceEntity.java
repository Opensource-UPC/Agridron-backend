package pe.com.agridron.shared.infrastructure.persistence.jpa.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base JPA persistence entity for all persistence entities.
 *
 * <p>Provides the surrogate primary key. This class lives in the infrastructure layer
 * to keep JPA concerns out of the domain model.</p>
 */
@Getter
@MappedSuperclass
public abstract class AbstractPersistenceEntity {

    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
