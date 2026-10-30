package pe.com.agridron.field.domain.model.aggregates;

import org.junit.jupiter.api.Test;
import pe.com.agridron.field.domain.events.FarmDetailsUpdatedEvent;
import pe.com.agridron.field.domain.events.FarmRegisteredEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorAssignedEvent;
import pe.com.agridron.field.domain.events.FarmSupervisorRemovedEvent;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FarmTest {

    @Test
    void registersFarmWithNormalizedText() {
        var farm = Farm.register(10L, "  Finca El Sol  ", "  Lima  ", "  Lima  ");

        assertThat(farm.getId()).isNull();
        assertThat(farm.getOwnerId()).isEqualTo(10L);
        assertThat(farm.getName()).isEqualTo("Finca El Sol");
        assertThat(farm.getLocation()).isEqualTo("Lima");
        assertThat(farm.getDepartment()).isEqualTo("Lima");
        assertThat(farm.getSupervisorIds()).isEmpty();
        assertThat(farm.domainEvents()).isEmpty();
    }

    @Test
    void onCreatedRegistersFarmRegisteredEventWithIdentity() {
        var farm = Farm.reconstitute(1L, 10L, "Finca El Sol", "Lima", "Lima", Set.of());

        farm.onCreated();

        assertThat(farm.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FarmRegisteredEvent.class, event -> {
                    assertThat(event.farmId()).isEqualTo(1L);
                    assertThat(event.ownerId()).isEqualTo(10L);
                    assertThat(event.name()).isEqualTo("Finca El Sol");
                });
    }

    @Test
    void assignsSupervisorAndRegistersEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        farm.assignSupervisor(20L);

        assertThat(farm.getSupervisorIds()).containsExactly(20L);
        assertThat(farm.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FarmSupervisorAssignedEvent.class, event -> {
                    assertThat(event.farmId()).isEqualTo(1L);
                    assertThat(event.supervisorId()).isEqualTo(20L);
                });
    }

    @Test
    void assigningTheSameSupervisorTwiceIsIdempotent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        farm.assignSupervisor(20L);
        farm.assignSupervisor(20L);

        assertThat(farm.getSupervisorIds()).containsExactly(20L);
        assertThat(farm.domainEvents()).hasSize(1);
    }

    @Test
    void removesSupervisorAndRegistersEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(20L));

        farm.removeSupervisor(20L);

        assertThat(farm.getSupervisorIds()).isEmpty();
        assertThat(farm.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FarmSupervisorRemovedEvent.class, event -> {
                    assertThat(event.farmId()).isEqualTo(1L);
                    assertThat(event.supervisorId()).isEqualTo(20L);
                });
    }

    @Test
    void removingAnAbsentSupervisorIsIdempotent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        farm.removeSupervisor(20L);

        assertThat(farm.domainEvents()).isEmpty();
    }

    @Test
    void updateDetailsNormalizesTextAndRegistersEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        farm.updateDetails("  Nueva Finca ", "  Ica ", "  Ica ");

        assertThat(farm.getName()).isEqualTo("Nueva Finca");
        assertThat(farm.getLocation()).isEqualTo("Ica");
        assertThat(farm.getDepartment()).isEqualTo("Ica");
        assertThat(farm.domainEvents()).singleElement()
                .isInstanceOfSatisfying(FarmDetailsUpdatedEvent.class, event -> {
                    assertThat(event.farmId()).isEqualTo(1L);
                    assertThat(event.ownerId()).isEqualTo(10L);
                    assertThat(event.name()).isEqualTo("Nueva Finca");
                    assertThat(event.location()).isEqualTo("Ica");
                    assertThat(event.department()).isEqualTo("Ica");
                });
    }

    @Test
    void updatingDetailsWithoutChangesDoesNotRegisterEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        farm.updateDetails("  Finca ", " Lima ", "Lima");

        assertThat(farm.domainEvents()).isEmpty();
    }

    @Test
    void supervisorIdsAreUnmodifiable() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(20L));

        assertThatThrownBy(() -> farm.getSupervisorIds().add(30L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsInvalidOwnerId() {
        assertThatThrownBy(() -> Farm.register(0L, "Finca", "Lima", "Lima"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ownerId");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Farm.register(10L, "   ", "Lima", "Lima"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsTooLongName() {
        assertThatThrownBy(() -> Farm.register(10L, "a".repeat(121), "Lima", "Lima"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("120");
    }

    @Test
    void rejectsBlankLocation() {
        assertThatThrownBy(() -> Farm.register(10L, "Finca", " ", "Lima"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("location");
    }

    @Test
    void rejectsBlankDepartment() {
        assertThatThrownBy(() -> Farm.register(10L, "Finca", "Lima", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("department");
    }

    @Test
    void rejectsInvalidSupervisorId() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of());

        assertThatThrownBy(() -> farm.assignSupervisor(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supervisorId");
    }
}
