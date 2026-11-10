package pe.com.agridron.analytics.infrastructure.persistence.jpa.assemblers;

import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.PerformanceIndicatorPersistenceEntity;

public final class PerformanceIndicatorPersistenceAssembler {

    private PerformanceIndicatorPersistenceAssembler() {
    }

    public static PerformanceIndicator toDomainFromPersistence(PerformanceIndicatorPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return PerformanceIndicator.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getValue(),
                entity.getUnit(),
                entity.getPeriod(),
                entity.getCalculatedAt());
    }

    public static PerformanceIndicatorPersistenceEntity toPersistenceFromDomain(PerformanceIndicator indicator) {
        if (indicator == null) {
            return null;
        }
        var entity = new PerformanceIndicatorPersistenceEntity(
                indicator.getName(),
                indicator.getValue(),
                indicator.getUnit(),
                indicator.getPeriod(),
                indicator.getCalculatedAt());
        entity.setId(indicator.getId());
        return entity;
    }
}
