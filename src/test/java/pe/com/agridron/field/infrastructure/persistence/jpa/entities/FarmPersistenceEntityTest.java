package pe.com.agridron.field.infrastructure.persistence.jpa.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FarmPersistenceEntityTest extends AbstractFieldPersistenceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsFarmWithAuditingFields() {
        var farm = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");

        var persisted = entityManager.persistFlushFind(farm);

        assertThat(persisted.getId()).isNotNull();
        assertThat(persisted.getOwnerId()).isEqualTo(10L);
        assertThat(persisted.getName()).isEqualTo("Finca El Sol");
        assertThat(persisted.getLocation()).isEqualTo("Lima");
        assertThat(persisted.getDepartment()).isEqualTo("Lima");
        assertThat(persisted.getCreatedAt()).isNotNull();
        assertThat(persisted.getUpdatedAt()).isNotNull();
        assertThat(persisted.getVersion()).isZero();
    }

    @Test
    void persistsAssignedSupervisors() {
        var farm = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        farm.replaceSupervisors(new LinkedHashSet<>(Set.of(20L, 30L)));

        var persisted = entityManager.persistFlushFind(farm);
        entityManager.clear();

        var reloaded = entityManager.find(FarmPersistenceEntity.class, persisted.getId());
        assertThat(reloaded.getSupervisorIds()).containsExactlyInAnyOrder(20L, 30L);
    }

    @Test
    void removingFarmDeletesItsSupervisors() {
        var farm = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        farm.replaceSupervisors(Set.of(20L, 30L));
        var persisted = entityManager.persistFlushFind(farm);

        entityManager.remove(persisted);
        entityManager.flush();

        var remaining = ((Number) entityManager.getEntityManager()
                .createNativeQuery("select count(*) from farm_supervisor where farm_id = :farmId")
                .setParameter("farmId", persisted.getId())
                .getSingleResult()).longValue();
        assertThat(remaining).isZero();
    }

    @Test
    void replacingSupervisorsWithNullEmptiesTheCollection() {
        var farm = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        farm.replaceSupervisors(Set.of(20L));

        farm.replaceSupervisors(null);

        assertThat(farm.getSupervisorIds()).isEmpty();
    }

    @Test
    void supervisorIdsExposedByEntityAreUnmodifiable() {
        var farm = new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima");
        farm.replaceSupervisors(Collections.singleton(20L));

        assertThat(farm.getSupervisorIds()).isUnmodifiable();
    }
}
