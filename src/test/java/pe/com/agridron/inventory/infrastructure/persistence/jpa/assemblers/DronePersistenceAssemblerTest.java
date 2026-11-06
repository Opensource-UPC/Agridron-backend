package pe.com.agridron.inventory.infrastructure.persistence.jpa.assemblers;


import org.junit.jupiter.api.Test;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.inventory.infrastructure.persistence.jpa.entities.DronePersistenceEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DronePersistenceAssemblerTest {

    @Test
    void mapsANewDroneToARowWithoutId() {
        var drone = Drone.register("DRN-2026-0101", "RTX-6030", 36, new NozzleId(3L), 35.0,
                LocalDate.of(2026, 11, 4), LocalDate.of(2027, 1, 5), "https://img/drone.png");

        var entity = DronePersistenceAssembler.toPersistenceFromDomain(drone);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getSerialNumber()).isEqualTo("DRN-2026-0101");
        assertThat(entity.getStatus()).isEqualTo(DroneStatus.AVAILABLE);
        assertThat(entity.getNozzleId()).isEqualTo(3L);
        assertThat(entity.getFlightHours()).isZero();
        assertThat(entity.getLastMaintenanceDate()).isEqualTo(LocalDate.of(2026, 11, 4));
        assertThat(entity.getUrlimg()).isEqualTo("https://img/drone.png");
    }

    @Test
    void mapsARowBackToTheAggregate() {
        var entity = new DronePersistenceEntity("DRN-6", "RTX-6030", 0, DroneStatus.OUT_OF_SERVICE,
                4L, 30.0, 3, null, null, null);
        entity.setId(6L);

        var drone = DronePersistenceAssembler.toDomainFromPersistence(entity);

        assertThat(drone.getId()).isEqualTo(6L);
        assertThat(drone.getStatus()).isEqualTo(DroneStatus.OUT_OF_SERVICE);
        assertThat(drone.getNozzleId()).isEqualTo(new NozzleId(4L));
        assertThat(drone.getFlightHours()).isEqualTo(3);
        assertThat(drone.getLastMaintenanceDate()).isNull();
    }
}