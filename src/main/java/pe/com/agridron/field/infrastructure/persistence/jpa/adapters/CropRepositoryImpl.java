package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.CropPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.CropPersistenceRepository;

import java.util.Optional;

/**
 * Repository adapter that bridges the read-only crop catalog port with Spring Data JPA.
 */
@Repository
public class CropRepositoryImpl implements CropRepository {

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("name"),
            Sort.Order.asc("id"));

    private final CropPersistenceRepository cropPersistenceRepository;

    public CropRepositoryImpl(CropPersistenceRepository cropPersistenceRepository) {
        this.cropPersistenceRepository = cropPersistenceRepository;
    }

    @Override
    public Optional<Crop> findById(Long id) {
        return cropPersistenceRepository.findById(id)
                .map(CropPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public PageResult<Crop> findAll(Pagination pagination, SortOrder sortOrder) {
        var sort = toSort(sortOrder);
        if (!pagination.paged()) {
            var content = cropPersistenceRepository.findAll(sort).stream()
                    .map(CropPersistenceAssembler::toDomainFromPersistence)
                    .toList();
            return PageResult.unpaged(content);
        }

        var page = cropPersistenceRepository.findAll(
                PageRequest.of(pagination.page(), pagination.size(), sort));
        return toPageResult(page);
    }

    @Override
    public boolean existsById(Long id) {
        return cropPersistenceRepository.existsById(id);
    }

    private static Sort toSort(SortOrder sortOrder) {
        if (sortOrder == null) {
            return DEFAULT_SORT;
        }
        var direction = sortOrder.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        var primary = Sort.by(new Sort.Order(direction, sortOrder.field()));
        return "id".equals(sortOrder.field())
                ? primary
                : primary.and(Sort.by(Sort.Order.asc("id")));
    }

    private static PageResult<Crop> toPageResult(Page<CropPersistenceEntity> page) {
        return new PageResult<>(
                page.getContent().stream()
                        .map(CropPersistenceAssembler::toDomainFromPersistence)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }
}
