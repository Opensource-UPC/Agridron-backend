package pe.com.agridron.field.infrastructure.persistence.jpa.assemblers;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FarmPersistenceAssemblerTest {

    @Test
    void rebuildsDomainFarmFromEntity() {
        var entity = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        entity.setId(7L);
        entity.replaceSupervisors(Set.of(20L, 30L));

        var farm = FarmPersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(farm.getId()).isEqualTo(7L);
        assertThat(farm.getOwnerId()).isEqualTo(10L);
        assertThat(farm.getName()).isEqualTo("Finca El Sol");
        assertThat(farm.getLocation()).isEqualTo("Lima");
        assertThat(farm.getDepartment()).isEqualTo("Lima");
        assertThat(farm.getSupervisorIds()).containsExactlyInAnyOrder(20L, 30L);
    }

    @Test
    void buildsEntityFromDomainFarm() {
        var farm = Farm.reconstitute(7L, 10L, "Finca El Sol", "Lima", "Lima", Set.of(20L));

        var entity = FarmPersistenceAssembler.toPersistenceFromDomain(farm);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getOwnerId()).isEqualTo(10L);
        assertThat(entity.getName()).isEqualTo("Finca El Sol");
        assertThat(entity.getSupervisorIds()).containsExactly(20L);
    }

    @Test
    void copiesMutableStateIntoAnExistingEntity() {
        var farm = Farm.reconstitute(7L, 10L, "Nueva Finca", "Ica", "Ica", Set.of(40L));
        var entity = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        entity.setId(7L);
        entity.replaceSupervisors(Set.of(20L));

        FarmPersistenceAssembler.copyToPersistence(farm, entity);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getName()).isEqualTo("Nueva Finca");
        assertThat(entity.getLocation()).isEqualTo("Ica");
        assertThat(entity.getDepartment()).isEqualTo("Ica");
        assertThat(entity.getSupervisorIds()).containsExactly(40L);
    }

    @Test
    void returnsNullWhenSourceIsNull() {
        assertThat(FarmPersistenceAssembler.toDomainFromPersistence(null)).isNull();
        assertThat(FarmPersistenceAssembler.toPersistenceFromDomain(null)).isNull();
    }
}
