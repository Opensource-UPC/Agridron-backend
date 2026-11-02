package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FumigationAreaPersistenceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class FumigationAreaPersistenceAssemblerTest {

    @Test
    void rebuildsDomainAreaFromEntity() {
        var geometry = geometry(0.001);
        var entity = new FumigationAreaPersistenceEntity(
                1L, geometry.polygon(), geometry.areaHectares());
        entity.setId(7L);

        var area = FumigationAreaPersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(area.getId()).isEqualTo(7L);
        assertThat(area.getParcelId()).isEqualTo(1L);
        assertThat(area.getGeometry().areaHectares()).isEqualTo(geometry.areaHectares());
    }

    @Test
    void buildsEntityFromDomainArea() {
        var geometry = geometry(0.001);
        var area = FumigationArea.reconstitute(7L, 1L, geometry);

        var entity = FumigationAreaPersistenceAssembler.toPersistenceFromDomain(area);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getParcelId()).isEqualTo(1L);
        assertThat(entity.getGeometry().equalsExact(geometry.polygon())).isTrue();
        assertThat(entity.getAreaHectares()).isEqualTo(geometry.areaHectares());
    }

    @Test
    void copiesMutableStateIntoAnExistingEntity() {
        var original = geometry(0.001);
        var updated = geometry(0.002);
        var area = FumigationArea.reconstitute(7L, 1L, updated);
        var entity = new FumigationAreaPersistenceEntity(
                1L, original.polygon(), original.areaHectares());
        entity.setId(7L);

        FumigationAreaPersistenceAssembler.copyToPersistence(area, entity);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getGeometry().equalsExact(updated.polygon())).isTrue();
        assertThat(entity.getAreaHectares()).isEqualTo(updated.areaHectares());
    }

    @Test
    void returnsNullWhenSourceIsNull() {
        assertThat(FumigationAreaPersistenceAssembler.toDomainFromPersistence(null)).isNull();
        assertThat(FumigationAreaPersistenceAssembler.toPersistenceFromDomain(null)).isNull();
    }

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, size));
    }
}
