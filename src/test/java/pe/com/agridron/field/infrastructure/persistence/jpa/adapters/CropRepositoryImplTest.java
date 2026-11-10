package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
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
        var persisted = entityManager.persistFlushFind(
                new CropPersistenceEntity("Café de prueba", "Catimor de prueba"));

        var crop = cropRepository.findById(persisted.getId());

        assertThat(crop).isPresent();
        assertThat(crop.get().getId()).isEqualTo(persisted.getId());
        assertThat(crop.get().getName()).isEqualTo("Café de prueba");
        assertThat(crop.get().getVariety()).isEqualTo("Catimor de prueba");
    }

    @Test
    void returnsEmptyWhenCropDoesNotExist() {
        assertThat(cropRepository.findById(999_999L)).isEmpty();
    }

    @Test
    void listsAllCropsIncludingTheSeededCatalog() {
        entityManager.persist(new CropPersistenceEntity("Cultivo A", "Variedad A"));
        entityManager.persist(new CropPersistenceEntity("Cultivo B", "Variedad B"));
        entityManager.flush();

        assertThat(cropRepository.findAll(Pagination.unpaged(), null).content())
                .extracting(Crop::getName)
                .contains("Cultivo A", "Cultivo B", "Café", "Papa", "Palta");
    }

    @Test
    void paginatesAndOrdersTheCatalog() {
        entityManager.persist(new CropPersistenceEntity("Zzz cultivo", "Variedad Z"));
        entityManager.persist(new CropPersistenceEntity("Cultivo B", "Variedad B"));
        entityManager.flush();

        var page = cropRepository.findAll(
                Pagination.of(0, 1),
                new SortOrder("name", SortDirection.DESC));

        assertThat(page.content()).extracting(Crop::getName).containsExactly("Zzz cultivo");
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(1);
        assertThat(page.totalElements()).isEqualTo(13);
        assertThat(page.totalPages()).isEqualTo(13);
        assertThat(page.last()).isFalse();
    }

    @Test
    void reportsExistence() {
        var persisted = entityManager.persistFlushFind(
                new CropPersistenceEntity("Arroz de prueba", "IR-43 de prueba"));

        assertThat(cropRepository.existsById(persisted.getId())).isTrue();
        assertThat(cropRepository.existsById(999_999L)).isFalse();
    }
}
