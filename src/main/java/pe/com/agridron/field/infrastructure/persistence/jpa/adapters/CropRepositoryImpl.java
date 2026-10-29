package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.springframework.stereotype.Repository;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.field.infrastructure.persistence.jpa.assemblers.CropPersistenceAssembler;
import pe.com.agridron.field.infrastructure.persistence.jpa.repositories.CropPersistenceRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter that bridges the read-only crop catalog port with Spring Data JPA.
 */
@Repository
public class CropRepositoryImpl implements CropRepository {

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
    public List<Crop> findAll() {
        return cropPersistenceRepository.findAll().stream()
                .map(CropPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public boolean existsById(Long id) {
        return cropPersistenceRepository.existsById(id);
    }
}
