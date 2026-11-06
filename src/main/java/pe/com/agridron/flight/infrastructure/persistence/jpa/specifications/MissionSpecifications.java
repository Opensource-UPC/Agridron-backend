package pe.com.agridron.flight.infrastructure.persistence.jpa.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.com.agridron.flight.domain.model.valueobjects.MissionStatus;
import pe.com.agridron.flight.domain.model.valueobjects.ParcelId;
import pe.com.agridron.flight.infrastructure.persistence.jpa.entities.MissionPersistenceEntity;

import java.util.ArrayList;

/**
 * Builds the JPA specification for the optional mission listing filters.
 */
public final class MissionSpecifications {

    private MissionSpecifications() {
    }

    /**
     * Combines every non-null filter with AND. Without filters it matches every mission.
     */
    public static Specification<MissionPersistenceEntity> withFilters(Long parcelId, MissionStatus status) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            if (parcelId != null) {
                predicates.add(criteriaBuilder.equal(root.get("parcelId"), new ParcelId(parcelId)));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
