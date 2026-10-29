package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.Farm;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link Farm} aggregate.
 */
public interface FarmRepository {

    Farm save(Farm farm);

    Optional<Farm> findById(Long id);

    List<Farm> findAllByOwnerId(Long ownerId);

    boolean existsByOwnerIdAndName(Long ownerId, String name);

    boolean existsByOwnerIdAndNameAndIdNot(Long ownerId, String name, Long id);
}
