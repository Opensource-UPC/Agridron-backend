package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.repositories.PerformanceIndicatorRepository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers.PerformanceIndicatorPersistenceAssembler;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories.PerformanceIndicatorPersistenceRepository;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

@Repository
public class PerformanceIndicatorRepositoryImpl implements PerformanceIndicatorRepository {

    private static final Sort ALL_ORDER = Sort.by(
            Sort.Order.desc("period"),
            Sort.Order.asc("name"));

    private final PerformanceIndicatorPersistenceRepository performanceIndicatorPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PerformanceIndicatorRepositoryImpl(
            PerformanceIndicatorPersistenceRepository performanceIndicatorPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.performanceIndicatorPersistenceRepository = performanceIndicatorPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public PerformanceIndicator save(PerformanceIndicator indicator) {
        var isNew = indicator.getId() == null;
        var entity = isNew
                ? PerformanceIndicatorPersistenceAssembler.toPersistenceFromDomain(indicator)
                : performanceIndicatorPersistenceRepository.findById(indicator.getId())
                        .orElseGet(() -> PerformanceIndicatorPersistenceAssembler.toPersistenceFromDomain(indicator));
        if (!isNew) {
            entity.update(indicator.getValue(), indicator.getCalculatedAt());
        }
        var savedEntity = performanceIndicatorPersistenceRepository.saveAndFlush(entity);
        var savedIndicator = PerformanceIndicatorPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedIndicator.onCreated();
            domainEventPublisher.publishAndClearEvents(savedIndicator);
        }
        domainEventPublisher.publishAndClearEvents(indicator);
        return savedIndicator;
    }

    @Override
    public Optional<PerformanceIndicator> findByNameAndPeriod(String name, String period) {
        return performanceIndicatorPersistenceRepository.findByNameAndPeriod(name, period)
                .map(PerformanceIndicatorPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<PerformanceIndicator> findAll() {
        return performanceIndicatorPersistenceRepository.findAll(ALL_ORDER).stream()
                .map(PerformanceIndicatorPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public List<PerformanceIndicator> findByPeriod(String period) {
        return performanceIndicatorPersistenceRepository.findAllByPeriodOrderByNameAsc(period).stream()
                .map(PerformanceIndicatorPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }
}
