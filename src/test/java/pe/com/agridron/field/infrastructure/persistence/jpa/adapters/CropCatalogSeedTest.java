package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@Import(CropRepositoryImpl.class)
class CropCatalogSeedTest extends AbstractFieldPersistenceTest {

    @Autowired
    private CropRepositoryImpl cropRepository;

    @Test
    void seedsTheExpectedCropCatalog() {
        assertThat(cropRepository.findAll())
                .extracting(Crop::getName)
                .contains(
                        "Café",
                        "Papa",
                        "Arroz",
                        "Maíz amarillo",
                        "Uva",
                        "Palta",
                        "Cacao",
                        "Caña de azúcar",
                        "Espárrago",
                        "Quinua",
                        "Arándano");
    }

    @Test
    void seedsTheExpectedVarieties() {
        assertThat(cropRepository.findAll())
                .extracting(Crop::getName, Crop::getVariety)
                .contains(
                        tuple("Café", "Catimor"),
                        tuple("Papa", "Única"),
                        tuple("Arroz", "IR-43"),
                        tuple("Palta", "Hass"),
                        tuple("Quinua", "Blanca de Junín"),
                        tuple("Arándano", "Biloxi"));
    }

    @Test
    void catalogEntriesAreReadableById() {
        var palta = cropRepository.findAll().stream()
                .filter(crop -> crop.getName().equals("Palta"))
                .findFirst()
                .orElseThrow();

        assertThat(cropRepository.findById(palta.getId()))
                .isPresent()
                .get()
                .extracting(Crop::getVariety)
                .isEqualTo("Hass");
        assertThat(cropRepository.existsById(palta.getId())).isTrue();
    }
}
