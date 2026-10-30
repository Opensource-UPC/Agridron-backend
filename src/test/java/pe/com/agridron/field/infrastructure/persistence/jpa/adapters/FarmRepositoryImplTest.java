package pe.com.agridron.field.infrastructure.persistence.jpa.adapters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import pe.com.agridron.field.domain.events.FarmDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.infrastructure.persistence.jpa.entities.FarmPersistenceEntity;
import pe.com.agridron.field.support.AbstractFieldPersistenceTest;
import pe.com.agridron.shared.infrastructure.events.DomainEventPublisher;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Import({FarmRepositoryImpl.class, DomainEventPublisher.class})
@RecordApplicationEvents
class FarmRepositoryImplTest extends AbstractFieldPersistenceTest {

    @Autowired
    private FarmRepositoryImpl farmRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesNewFarmAndPublishesCreationEvent() {
        var farm = Farm.register(10L, "Finca El Sol", "Lima", "Lima");

        var saved = farmRepository.save(farm);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(10L);
        assertThat(applicationEvents.stream(FarmRegisteredEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(saved.getId());
                    assertThat(event.ownerId()).isEqualTo(10L);
                    assertThat(event.name()).isEqualTo("Finca El Sol");
                });
    }

    @Test
    void findsFarmByIdWithItsSupervisors() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of(20L, 30L));

        var farm = farmRepository.findById(persisted.getId());

        assertThat(farm).isPresent();
        assertThat(farm.get().getName()).isEqualTo("Finca El Sol");
        assertThat(farm.get().getSupervisorIds()).containsExactlyInAnyOrder(20L, 30L);
    }

    @Test
    void returnsEmptyWhenFarmDoesNotExist() {
        assertThat(farmRepository.findById(999_999L)).isEmpty();
    }

    @Test
    void assigningSupervisorToExistingFarmPublishesEvent() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of());
        var farm = farmRepository.findById(persisted.getId()).orElseThrow();

        farm.assignSupervisor(20L);
        var saved = farmRepository.save(farm);

        assertThat(saved.getSupervisorIds()).containsExactly(20L);
        assertThat(applicationEvents.stream(FarmSupervisorAssignedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(persisted.getId());
                    assertThat(event.supervisorId()).isEqualTo(20L);
                });
    }

    @Test
    void removingSupervisorFromExistingFarmPublishesEvent() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of(20L));
        var farm = farmRepository.findById(persisted.getId()).orElseThrow();

        farm.removeSupervisor(20L);
        var saved = farmRepository.save(farm);

        assertThat(saved.getSupervisorIds()).isEmpty();
        assertThat(applicationEvents.stream(FarmSupervisorRemovedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(persisted.getId());
                    assertThat(event.supervisorId()).isEqualTo(20L);
                });
    }

    @Test
    void updatingDetailsOfExistingFarmPublishesEvent() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of());
        var farm = farmRepository.findById(persisted.getId()).orElseThrow();

        farm.updateDetails("Nueva Finca", "Ica", "Ica");
        var saved = farmRepository.save(farm);

        assertThat(saved.getName()).isEqualTo("Nueva Finca");
        assertThat(saved.getLocation()).isEqualTo("Ica");
        assertThat(applicationEvents.stream(FarmDetailsUpdatedEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(persisted.getId());
                    assertThat(event.ownerId()).isEqualTo(10L);
                    assertThat(event.name()).isEqualTo("Nueva Finca");
                    assertThat(event.location()).isEqualTo("Ica");
                    assertThat(event.department()).isEqualTo("Ica");
                });
    }

    @Test
    void updatingDetailsWithoutChangesDoesNotPublishEvents() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of());
        var farm = farmRepository.findById(persisted.getId()).orElseThrow();

        farm.updateDetails("Finca El Sol", "Lima", "Lima");
        farmRepository.save(farm);

        assertThat(applicationEvents.stream(FarmDetailsUpdatedEvent.class)).isEmpty();
        assertThat(applicationEvents.stream(FarmSupervisorAssignedEvent.class)).isEmpty();
        assertThat(applicationEvents.stream(FarmSupervisorRemovedEvent.class)).isEmpty();
    }

    @Test
    void listsFarmsOfAnOwnerOrderedByName() {
        persistFarm(10L, "Zeta", Set.of());
        persistFarm(10L, "Alfa", Set.of());
        persistFarm(20L, "Otra", Set.of());

        var farms = farmRepository.findAllByOwnerId(10L);

        assertThat(farms).extracting(Farm::getName).containsExactly("Alfa", "Zeta");
    }

    @Test
    void delegatesExistenceChecks() {
        var persisted = persistFarm(10L, "Finca El Sol", Set.of());

        assertThat(farmRepository.existsByOwnerIdAndName(10L, "Finca El Sol")).isTrue();
        assertThat(farmRepository.existsByOwnerIdAndName(10L, "Otra")).isFalse();
        assertThat(farmRepository.existsByOwnerIdAndNameAndIdNot(10L, "Finca El Sol", persisted.getId()))
                .isFalse();
        assertThat(farmRepository.existsByOwnerIdAndNameAndIdNot(10L, "Finca El Sol", 999_999L)).isTrue();
    }

    private FarmPersistenceEntity persistFarm(Long ownerId, String name, Set<Long> supervisorIds) {
        var entity = new FarmPersistenceEntity(ownerId, name, "Lima", "Lima");
        entity.replaceSupervisors(supervisorIds);
        var persisted = entityManager.persistFlushFind(entity);
        entityManager.clear();
        return persisted;
    }
}
