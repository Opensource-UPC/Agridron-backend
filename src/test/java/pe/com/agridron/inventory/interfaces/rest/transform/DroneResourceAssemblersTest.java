package pe.com.agridron.inventory.interfaces.rest.transform;

import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.inventory.interfaces.rest.resources.CreateDroneResource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DroneResourceAssemblersTest {

    @Test
    void buildsTheCommandAndParsesIsoDates() {
        var resource = new CreateDroneResource("DRN-1", "RTX-6030", 80, 1L, 35.0,
                "2026-11-04", "2027-01-05", null);

        var command = CreateDroneCommandFromResourceAssembler.toCommandFromResource(resource);

        assertThat(command.serialNumber()).isEqualTo("DRN-1");
        assertThat(command.nozzleId()).isEqualTo(1L);
        assertThat(command.lastMaintenanceDate()).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(command.nextMaintenanceDate()).isEqualTo(LocalDate.of(2027, 1, 5));
    }

    @Test
    void emptyDatesBecomeNull() {
        var resource = new CreateDroneResource("DRN-7", "RTX-6030", 50, 4L, 20.0, "", null, null);

        var command = CreateDroneCommandFromResourceAssembler.toCommandFromResource(resource);

        assertThat(command.lastMaintenanceDate()).isNull();
        assertThat(command.nextMaintenanceDate()).isNull();
    }

    @Test
    void rejectsDatesInAnotherFormat() {
        var resource = new CreateDroneResource("DRN-1", "RTX-6030", 80, 1L, 35.0, "04/11/2026", null, null);

        assertThatThrownBy(() -> CreateDroneCommandFromResourceAssembler.toCommandFromResource(resource))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buildsTheResourceWithFrontendStatusAndIsoDates() {
        var drone = Drone.reconstitute(1L, "DRN-1", "RTX-6030", 36, DroneStatus.OUT_OF_SERVICE,
                new NozzleId(3L), 35.0, 4, LocalDate.of(2026, 11, 4), null, "https://img/d.png");

        var resource = DroneResourceFromEntityAssembler.toResourceFromEntity(drone);

        assertThat(resource.id()).isEqualTo(1L);
        assertThat(resource.status()).isEqualTo("out-of-service");
        assertThat(resource.nozzleId()).isEqualTo(3L);
        assertThat(resource.lastMaintenanceDate()).isEqualTo("2026-11-04");
        assertThat(resource.nextMaintenanceDate()).isNull();
        assertThat(resource.flightHours()).isEqualTo(4);
    }
}