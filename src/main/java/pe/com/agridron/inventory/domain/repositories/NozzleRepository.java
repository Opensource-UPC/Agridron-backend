package pe.com.agridron.inventory.domain.repositories;


import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio del agregado {@link Nozzle}.
 *
 * <p>Es una interfaz del dominio: no sabe nada de JPA. La implementa un adaptador en
 * {@code infrastructure/persistence/jpa/adapters}.</p>
 */
public interface NozzleRepository {

    Nozzle save(Nozzle nozzle);

    Optional<Nozzle> findById(Long id);

    List<Nozzle> findAll();

    boolean existsById(Long id);
}