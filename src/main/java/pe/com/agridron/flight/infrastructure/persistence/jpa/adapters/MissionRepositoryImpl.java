package pe.com.agridron.flight.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.flight.domain.model.aggregates.Mission;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.flight.domain.repositories.MissionRepository;
import pe.com.agridron.flight.infrastructure.persistence.jpa.assemblers.MissionPersistenceAssembler;
import pe.com.agridron.flight.infrastructure.persistence.jpa.repositories.MissionPersistenceRepository;
import pe.com.agridron.flight.infrastructure.persistence.jpa.specifications.MissionSpecifications;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the mission repository port with Spring Data JPA.
 *
 * <p>Acts as the event-publishing boundary: a brand-new mission gets its identity from the
 * database, then {@code onCreated()} registers the creation event. Events registered on the
 * aggregate before saving an existing mission are published too. The adapter flushes before
 * rebuilding the aggregate so that new child entities come back with their identities.</p>
 */
@Repository
public class MissionRepositoryImpl implements MissionRepository {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("requestedDate"),
            Sort.Order.asc("id"));

    private final MissionPersistenceRepository missionPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MissionRepositoryImpl(
            MissionPersistenceRepository missionPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.missionPersistenceRepository = missionPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public Mission save(Mission mission) {
        var isNew = mission.getId() == null;
        var entity = isNew
                ? missionPersistenceRepository.save(MissionPersistenceAssembler.toPersistenceFromDomain(mission))
                : missionPersistenceRepository.findForUpdateById(mission.getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Mission %d disappeared while being updated".formatted(mission.getId())));

        if (!isNew) {
            MissionPersistenceAssembler.copyToPersistence(mission, entity);
        }
        missionPersistenceRepository.flush();

        var savedMission = MissionPersistenceAssembler.toDomainFromPersistence(entity);
        if (isNew) {
            savedMission.onCreated();
            domainEventPublisher.publishAndClearEvents(savedMission);
        }
        domainEventPublisher.publishAndClearEvents(mission);
        return savedMission;
    }

    @Override
    public Optional<Mission> findById(Long id) {
        return missionPersistenceRepository.findById(id)
                .map(MissionPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Mission> findAll(Long parcelId, MissionStatus status) {
        return missionPersistenceRepository
                .findAll(MissionSpecifications.withFilters(parcelId, status), DEFAULT_SORT)
                .stream()
                .map(MissionPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByParcelIdAndStatusIn(Long parcelId, Collection<MissionStatus> statuses) {
        return missionPersistenceRepository.existsByParcelIdAndStatusIn(new ParcelId(parcelId), statuses);
    }
}
