package pe.com.agridron.inventory.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DroneStatusResourceAssemblerTest {

    @Test
    void writesStatusesInTheFrontendFormat() {
        assertThat(DroneStatusResourceAssembler.toResourceFromStatus(DroneStatus.AVAILABLE)).isEqualTo("available");
        assertThat(DroneStatusResourceAssembler.toResourceFromStatus(DroneStatus.IN_FLIGHT)).isEqualTo("in-flight");
        assertThat(DroneStatusResourceAssembler.toResourceFromStatus(DroneStatus.OUT_OF_SERVICE)).isEqualTo("out-of-service");
    }

    @Test
    void readsBothFormats() {
        assertThat(DroneStatusResourceAssembler.toStatusFromResource("in-flight")).isEqualTo(DroneStatus.IN_FLIGHT);
        assertThat(DroneStatusResourceAssembler.toStatusFromResource(" OUT_OF_SERVICE ")).isEqualTo(DroneStatus.OUT_OF_SERVICE);
    }

    @Test
    void rejectsUnknownStatuses() {
        assertThatThrownBy(() -> DroneStatusResourceAssembler.toStatusFromResource("flying"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DroneStatusResourceAssembler.toStatusFromResource(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
