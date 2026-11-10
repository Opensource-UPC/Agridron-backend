package pe.com.agridron.analytics.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.domain.repositories.MissionReportRepository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers.MissionReportPersistenceAssembler;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.repositories.MissionReportPersistenceRepository;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.specifications.MissionReportSpecifications;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.List;
import java.util.Optional;

@Repository
public class MissionReportRepositoryImpl implements MissionReportRepository {

    private static final Sort SEARCH_ORDER = Sort.by(
            Sort.Order.desc("generatedAt"),
            Sort.Order.desc("id"));

    private final MissionReportPersistenceRepository missionReportPersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public MissionReportRepositoryImpl(
            MissionReportPersistenceRepository missionReportPersistenceRepository,
            DomainEventPublisher domainEventPublisher) {
        this.missionReportPersistenceRepository = missionReportPersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public MissionReport save(MissionReport report) {
        var isNew = report.getId() == null;
        var entity = isNew
                ? MissionReportPersistenceAssembler.toPersistenceFromDomain(report)
                : missionReportPersistenceRepository.findById(report.getId())
                        .orElseGet(() -> MissionReportPersistenceAssembler.toPersistenceFromDomain(report));
        if (!isNew) {
            MissionReportPersistenceAssembler.copyNewMetricsToPersistence(report, entity);
        }
        var savedEntity = missionReportPersistenceRepository.saveAndFlush(entity);
        var savedReport = MissionReportPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedReport.onCreated();
            domainEventPublisher.publishAndClearEvents(savedReport);
        }
        domainEventPublisher.publishAndClearEvents(report);
        return savedReport;
    }

    @Override
    public Optional<MissionReport> findById(Long id) {
        return missionReportPersistenceRepository.findById(id)
                .map(MissionReportPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<MissionReport> search(MissionReportSearchCriteria criteria) {
        return missionReportPersistenceRepository
                .findAll(MissionReportSpecifications.withCriteria(criteria), SEARCH_ORDER)
                .stream()
                .map(MissionReportPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsByMissionIdAndType(Long missionId, ReportType type) {
        return missionReportPersistenceRepository.existsByMissionIdAndType(missionId, type);
    }

    @Override
    public boolean existsById(Long id) {
        return missionReportPersistenceRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        missionReportPersistenceRepository.deleteById(id);
    }
}
