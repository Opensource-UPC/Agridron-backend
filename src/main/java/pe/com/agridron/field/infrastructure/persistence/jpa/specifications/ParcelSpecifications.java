package pe.com.agridron.field.infrastructure.persistence.jpa.specifications;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.ParcelPersistenceEntity;

import java.util.ArrayList;

/**
 * Builds the JPA specification for the optional parcel search filters (FM-005).
 */
public final class ParcelSpecifications {

    private ParcelSpecifications() {
    }

    /**
     * Combines every non-null criterion with AND. An empty criteria matches all parcels.
     *
     * @param criteria the search filters
     * @return a specification ready to be used with {@code findAll(specification, sort)}
     */
    public static Specification<ParcelPersistenceEntity> withCriteria(ParcelSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<Predicate>();
            if (criteria.farmId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("farmId"), criteria.farmId()));
            }
            if (criteria.status() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.status()));
            }
            if (criteria.cropId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("cropId"), criteria.cropId()));
            }
            if (criteria.minAreaHectares() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("areaHectares"), criteria.minAreaHectares()));
            }
            if (criteria.maxAreaHectares() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("areaHectares"), criteria.maxAreaHectares()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
