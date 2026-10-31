package pe.com.agridron.analytics.infrastructure.persistence.jpa.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.com.agridron.analytics.domain.model.queries.MissionHistorySearchCriteria;
import pe.com.agridron.analytics.infrastructure.persistence.jpa.entities.MissionHistoryPersistenceEntity;

import java.util.ArrayList;

public final class MissionHistorySpecifications {

    private MissionHistorySpecifications() {
    }

    public static Specification<MissionHistoryPersistenceEntity> withCriteria(MissionHistorySearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            if (criteria.missionIds() != null) {
                predicates.add(root.get("missionId").in(criteria.missionIds()));
            }
            if (criteria.parcelIds() != null) {
                predicates.add(root.get("parcelId").in(criteria.parcelIds()));
            }
            if (criteria.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("finalStatus"), criteria.status()));
            }
            if (criteria.from() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("completedAt"), criteria.from()));
            }
            if (criteria.to() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("completedAt"), criteria.to()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
