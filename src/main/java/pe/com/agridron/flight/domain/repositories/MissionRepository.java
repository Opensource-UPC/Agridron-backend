package pe.com.agridron.flight.domain.repositories;

import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link Mission} aggregate and its child entities.
 */
public interface MissionRepository {

    /**
     * Saves the mission and its child entities, and publishes the registered domain events.
     *
     * @return the saved mission, with every identity assigned
     */
    Mission save(Mission mission);

    Optional<Mission> findById(Long id);

    /**
     * Lists missions ordered by requested date and id.
     *
     * @param parcelId optional parcel filter, {@code null} for every parcel
     * @param status   optional status filter, {@code null} for every status
     */
    List<Mission> findAll(Long parcelId, MissionStatus status);

    boolean existsByParcelIdAndStatusIn(Long parcelId, Collection<MissionStatus> statuses);
}
