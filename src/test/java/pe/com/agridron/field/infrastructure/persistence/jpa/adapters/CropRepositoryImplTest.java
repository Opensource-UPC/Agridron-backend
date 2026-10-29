package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import static org.assertj.core.api.Assertions.assertThat;

@Import(CropRepositoryImpl.class)
class CropRepositoryImplTest extends AbstractFieldPersistenceTest {

    @Autowired
    private CropRepositoryImpl cropRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findsCropById() {
        var persisted = entityManager.persistFlushFind(new CropPersistenceEntity("Café", "Catimor"));

        var crop = cropRepository.findById(persisted.getId());

        assertThat(crop).isPresent();
        assertThat(crop.get().getId()).isEqualTo(persisted.getId());
        assertThat(crop.get().getName()).isEqualTo("Café");
        assertThat(crop.get().getVariety()).isEqualTo("Catimor");
    }

    @Test
    void returnsEmptyWhenCropDoesNotExist() {
        assertThat(cropRepository.findById(999_999L)).isEmpty();
    }

    @Test
    void listsAllCrops() {
        entityManager.persist(new CropPersistenceEntity("Café", "Catimor"));
        entityManager.persist(new CropPersistenceEntity("Papa", "Única"));
        entityManager.flush();

        assertThat(cropRepository.findAll())
                .extracting("name")
                .containsExactlyInAnyOrder("Café", "Papa");
    }

    @Test
    void reportsExistence() {
        var persisted = entityManager.persistFlushFind(new CropPersistenceEntity("Arroz", "IR-43"));

        assertThat(cropRepository.existsById(persisted.getId())).isTrue();
        assertThat(cropRepository.existsById(999_999L)).isFalse();
    }
}
