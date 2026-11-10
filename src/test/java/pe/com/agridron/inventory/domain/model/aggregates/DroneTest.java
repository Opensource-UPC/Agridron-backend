package pe.com.agridron.inventory.domain.model.aggregates;


import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DroneTest {

    private static Drone registerWith(int batteryLevel, Double tankCapacityLiters) {
        return Drone.register("DRN-2026-0101", "RTX-6030", batteryLevel, new NozzleId(3L),
                tankCapacityLiters, null, null, null);
    }

    @Test
    void registersAnAvailableDroneWithZeroFlightHours() {
        var drone = Drone.register("  DRN-2026-0101 ", " RTX-6030 ", 36, new NozzleId(3L), 35.0,
                LocalDate.of(2026, 11, 4), LocalDate.of(2027, 1, 5), "https://img/drone.png");

        assertThat(drone.getId()).isNull();
        assertThat(drone.getSerialNumber()).isEqualTo("DRN-2026-0101");
        assertThat(drone.getModel()).isEqualTo("RTX-6030");
        assertThat(drone.getBatteryLevel()).isEqualTo(36);
        assertThat(drone.getStatus()).isEqualTo(DroneStatus.AVAILABLE);
        assertThat(drone.isAvailable()).isTrue();
        assertThat(drone.getNozzleId()).isEqualTo(new NozzleId(3L));
        assertThat(drone.getTankCapacityLiters()).isEqualTo(35.0);
        assertThat(drone.getFlightHours()).isZero();
        assertThat(drone.getLastMaintenanceDate()).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(drone.getNextMaintenanceDate()).isEqualTo(LocalDate.of(2027, 1, 5));
        assertThat(drone.getUrlimg()).isEqualTo("https://img/drone.png");
    }

    @Test
    void maintenanceDatesAndImageAreOptional() {
        var drone = Drone.register("DRN-7", "RTX-6030", 50, new NozzleId(4L), 20.0, null, null, "  ");

        assertThat(drone.getLastMaintenanceDate()).isNull();
        assertThat(drone.getNextMaintenanceDate()).isNull();
        assertThat(drone.getUrlimg()).isNull();
    }

    @Test
    void rejectsBatteryLevelOutsideZeroToHundred() {
        assertThatThrownBy(() -> registerWith(-1, 35.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registerWith(101, 35.0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(registerWith(0, 35.0).getBatteryLevel()).isZero();
        assertThat(registerWith(100, 35.0).getBatteryLevel()).isEqualTo(100);
    }

    @Test
    void rejectsNonPositiveTankCapacity() {
        assertThatThrownBy(() -> registerWith(50, 0.0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> registerWith(50, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMissingSerialNumberModelOrNozzle() {
        assertThatThrownBy(() -> Drone.register(" ", "RTX", 50, new NozzleId(1L), 10.0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Drone.register("DRN-1", null, 50, new NozzleId(1L), 10.0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Drone.register("DRN-1", "RTX", 50, null, 10.0, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNextMaintenanceBeforeLastMaintenance() {
        assertThatThrownBy(() -> Drone.register("DRN-1", "RTX", 50, new NozzleId(1L), 10.0,
                LocalDate.of(2026, 11, 4), LocalDate.of(2026, 10, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reconstituteKeepsIdStatusAndFlightHours() {
        var drone = Drone.reconstitute(6L, "DRN-6", "RTX", 0, DroneStatus.OUT_OF_SERVICE,
                new NozzleId(4L), 30.0, 3, null, null, null);

        assertThat(drone.getId()).isEqualTo(6L);
        assertThat(drone.getStatus()).isEqualTo(DroneStatus.OUT_OF_SERVICE);
        assertThat(drone.isAvailable()).isFalse();
        assertThat(drone.getFlightHours()).isEqualTo(3);
    }

    @Test
    void nozzleIdMustBePositive() {
        assertThatThrownBy(() -> new NozzleId(0L)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new NozzleId(null)).isInstanceOf(IllegalArgumentException.class);
    }
}