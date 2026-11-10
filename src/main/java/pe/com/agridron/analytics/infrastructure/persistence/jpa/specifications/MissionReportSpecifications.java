package pe.com.agridron.analytics.infrastructure.persistence.jpa.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionReportPersistenceEntity;

import java.util.ArrayList;

public final class MissionReportSpecifications {

    private MissionReportSpecifications() {
    }

    public static Specification<MissionReportPersistenceEntity> withCriteria(MissionReportSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            if (criteria.missionIds() != null) {
                predicates.add(root.get("missionId").in(criteria.missionIds()));
            }
            if (criteria.parcelIds() != null) {
                predicates.add(root.get("parcelId").in(criteria.parcelIds()));
            }
            if (criteria.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), criteria.type()));
            }
            if (criteria.from() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("generatedAt"), criteria.from()));
            }
            if (criteria.to() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("generatedAt"), criteria.to()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
