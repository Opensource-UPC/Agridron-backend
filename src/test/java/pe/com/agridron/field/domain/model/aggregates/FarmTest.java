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
        var farm = Farm.register(10L, "  Finca El Sol  ", "  Lima  ", "  Lima  ", null);

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
        var farm = Farm.reconstitute(1L, 10L, "Finca El Sol", "Lima", "Lima", Set.of(), null);

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
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

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
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        farm.assignSupervisor(20L);
        farm.assignSupervisor(20L);

        assertThat(farm.getSupervisorIds()).containsExactly(20L);
        assertThat(farm.domainEvents()).hasSize(1);
    }

    @Test
    void removesSupervisorAndRegistersEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(20L), null);

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
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        farm.removeSupervisor(20L);

        assertThat(farm.domainEvents()).isEmpty();
    }

    @Test
    void updateDetailsNormalizesTextAndRegistersEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        farm.updateDetails("  Nueva Finca ", "  Ica ", "  Ica ", null);

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
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        farm.updateDetails("  Finca ", " Lima ", "Lima", null);

        assertThat(farm.domainEvents()).isEmpty();
    }

    @Test
    void storesTrimmedImageAndNormalizesBlankToNull() {
        var withImage = Farm.register(10L, "Finca", "Lima", "Lima", "  https://example.com/farm.jpg  ");
        var withBlankImage = Farm.register(10L, "Finca", "Lima", "Lima", "   ");

        assertThat(withImage.getImage()).isEqualTo("https://example.com/farm.jpg");
        assertThat(withBlankImage.getImage()).isNull();
    }

    @Test
    void updatesImageAndRegistersDetailsEvent() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        farm.updateDetails("Finca", "Lima", "Lima", "https://example.com/farm.jpg");

        assertThat(farm.getImage()).isEqualTo("https://example.com/farm.jpg");
        assertThat(farm.domainEvents()).singleElement()
                .isInstanceOf(FarmDetailsUpdatedEvent.class);
    }

    @Test
    void rejectsTooLongImage() {
        assertThatThrownBy(() -> Farm.register(
                10L, "Finca", "Lima", "Lima", "a".repeat(501)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("500");
    }

    @Test
    void supervisorIdsAreUnmodifiable() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(20L), null);

        assertThatThrownBy(() -> farm.getSupervisorIds().add(30L))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void rejectsInvalidOwnerId() {
        assertThatThrownBy(() -> Farm.register(0L, "Finca", "Lima", "Lima", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ownerId");
    }

    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> Farm.register(10L, "   ", "Lima", "Lima", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void rejectsTooLongName() {
        assertThatThrownBy(() -> Farm.register(10L, "a".repeat(121), "Lima", "Lima", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("120");
    }

    @Test
    void rejectsBlankLocation() {
        assertThatThrownBy(() -> Farm.register(10L, "Finca", " ", "Lima", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("location");
    }

    @Test
    void rejectsBlankDepartment() {
        assertThatThrownBy(() -> Farm.register(10L, "Finca", "Lima", " ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("department");
    }

    @Test
    void rejectsInvalidSupervisorId() {
        var farm = Farm.reconstitute(1L, 10L, "Finca", "Lima", "Lima", Set.of(), null);

        assertThatThrownBy(() -> farm.assignSupervisor(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("supervisorId");
    }
}
