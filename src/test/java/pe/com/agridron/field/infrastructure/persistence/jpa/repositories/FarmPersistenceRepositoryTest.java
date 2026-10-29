package pe.com.agridron.field.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FarmPersistenceRepositoryTest extends AbstractFieldPersistenceTest {

    @Autowired
    private FarmPersistenceRepository farmPersistenceRepository;

    @Test
    void findsFarmsByOwnerOrderedByName() {
        farmPersistenceRepository.saveAndFlush(new FarmPersistenceEntity(10L, "Zeta", "Lima", "Lima"));
        farmPersistenceRepository.saveAndFlush(new FarmPersistenceEntity(10L, "Alfa", "Lima", "Lima"));
        farmPersistenceRepository.saveAndFlush(new FarmPersistenceEntity(20L, "Otra", "Lima", "Lima"));

        var farms = farmPersistenceRepository.findAllByOwnerIdOrderByNameAsc(10L);

        assertThat(farms).extracting(FarmPersistenceEntity::getName)
                .containsExactly("Alfa", "Zeta");
    }

    @Test
    void detectsDuplicatedNameForOwner() {
        var farm = farmPersistenceRepository.saveAndFlush(
                new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima"));

        assertThat(farmPersistenceRepository.existsByOwnerIdAndName(10L, "Finca El Sol")).isTrue();
        assertThat(farmPersistenceRepository.existsByOwnerIdAndName(10L, "Otra")).isFalse();
        assertThat(farmPersistenceRepository.existsByOwnerIdAndName(20L, "Finca El Sol")).isFalse();
        assertThat(farmPersistenceRepository.existsByOwnerIdAndNameAndIdNot(10L, "Finca El Sol", farm.getId()))
                .isFalse();
        assertThat(farmPersistenceRepository.existsByOwnerIdAndNameAndIdNot(10L, "Finca El Sol", 999_999L))
                .isTrue();
    }

    @Test
    void rejectsDuplicatedNameForSameOwner() {
        farmPersistenceRepository.saveAndFlush(new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima"));

        assertThatThrownBy(() -> farmPersistenceRepository.saveAndFlush(
                new FarmPersistenceEntity(10L, "Finca El Sol", "Ica", "Ica")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSameNameForDifferentOwners() {
        farmPersistenceRepository.saveAndFlush(new FarmPersistenceEntity(10L, "Finca El Sol", "Lima", "Lima"));

        var other = farmPersistenceRepository.saveAndFlush(
                new FarmPersistenceEntity(20L, "Finca El Sol", "Lima", "Lima"));

        assertThat(other.getId()).isNotNull();
    }
}
