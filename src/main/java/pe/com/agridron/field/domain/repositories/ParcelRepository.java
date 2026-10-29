package pe.com.agridron.field.domain.repositories;

import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the {@link Parcel} aggregate.
 */
public interface ParcelRepository {

    Parcel save(Parcel parcel);

    Optional<Parcel> findById(Long id);

    List<Parcel> findByFarmId(Long farmId);

    List<Parcel> search(ParcelSearchCriteria criteria);

    boolean existsByFarmIdAndName(Long farmId, String name);

    boolean existsByFarmIdAndNameAndIdNot(Long farmId, String name, Long id);
}
