package pe.com.agridron.inventory.domain.repositories;


import pe.com.agridron.inventory.domain.model.aggregates.Drone;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de repositorio del agregado {@link Drone}.
 *
 * <p>Es una interfaz del dominio: no sabe nada de JPA. La implementa un adaptador en
 * {@code infrastructure/persistence/jpa/adapters}.</p>
 */
public interface DroneRepository {

    Drone save(Drone drone);

    Optional<Drone> findById(Long id);

    List<Drone> findAll();

    boolean existsById(Long id);

    boolean existsBySerialNumber(String serialNumber);
}