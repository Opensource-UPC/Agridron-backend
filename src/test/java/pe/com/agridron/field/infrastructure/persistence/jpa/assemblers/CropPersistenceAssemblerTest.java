package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.CropPersistenceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CropPersistenceAssemblerTest {

    @Test
    void rebuildsDomainCropFromEntity() {
        var entity = new CropPersistenceEntity("Café", "Catimor");
        entity.setId(7L);

        var crop = CropPersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(crop.getId()).isEqualTo(7L);
        assertThat(crop.getName()).isEqualTo("Café");
        assertThat(crop.getVariety()).isEqualTo("Catimor");
    }

    @Test
    void buildsEntityFromDomainCrop() {
        var crop = Crop.reconstitute(7L, "Café", "Catimor");

        var entity = CropPersistenceAssembler.toPersistenceFromDomain(crop);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getName()).isEqualTo("Café");
        assertThat(entity.getVariety()).isEqualTo("Catimor");
    }

    @Test
    void returnsNullWhenSourceIsNull() {
        assertThat(CropPersistenceAssembler.toDomainFromPersistence(null)).isNull();
        assertThat(CropPersistenceAssembler.toPersistenceFromDomain(null)).isNull();
    }
}
