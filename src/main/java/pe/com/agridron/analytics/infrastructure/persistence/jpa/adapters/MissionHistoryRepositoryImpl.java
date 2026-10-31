package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.domain.model.aggregates.MissionHistory;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.domain.repositories.MissionHistoryRepository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers.MissionHistoryPersistenceAssembler;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories.MissionHistoryPersistenceRepository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.specifications.MissionHistorySpecifications;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

@Repository
public class MissionHistoryRepositoryImpl implements MissionHistoryRepository {

    private static final Sort SEARCH_ORDER = Sort.by(
            Sort.Order.desc("completedAt"),
            Sort.Order.desc("id"));

    private final MissionHistoryPersistenceRepository missionHistoryPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MissionHistoryRepositoryImpl(
            MissionHistoryPersistenceRepository missionHistoryPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.missionHistoryPersistenceRepository = missionHistoryPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public MissionHistory save(MissionHistory history) {
        var savedEntity = missionHistoryPersistenceRepository.saveAndFlush(
                MissionHistoryPersistenceAssembler.toPersistenceFromDomain(history));
        var savedHistory = MissionHistoryPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (history.getId() == null) {
            savedHistory.onCreated();
            domainEventPublisher.publishAndClearEvents(savedHistory);
        }
        domainEventPublisher.publishAndClearEvents(history);
        return savedHistory;
    }

    @Override
    public Optional<MissionHistory> findById(Long id) {
        return missionHistoryPersistenceRepository.findById(id)
                .map(MissionHistoryPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<MissionHistory> search(MissionHistorySearchCriteria criteria) {
        return missionHistoryPersistenceRepository
                .findAll(MissionHistorySpecifications.withCriteria(criteria), SEARCH_ORDER)
                .stream()
                .map(MissionHistoryPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByMissionId(Long missionId) {
        return missionHistoryPersistenceRepository.existsByMissionId(missionId);
    }
}
