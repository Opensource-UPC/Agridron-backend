package pe.com.agridron.inventory.application.internal.queryservices;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.queries.GetAllDronesQuery;
import pe.com.agridron.inventory.domain.model.queries.GetDroneByIdQuery;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleId;
import pe.com.agridron.inventory.domain.repositories.DroneRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DroneQueryServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @InjectMocks
    private DroneQueryServiceImpl service;

    @Test
    void returnsTheDroneWhenItExists() {
        var drone = Drone.reconstitute(1L, "DRN-1", "RTX-6030", 36, DroneStatus.AVAILABLE, new NozzleId(3L), 35.0, 4, null, null, null);
        when(droneRepository.findById(1L)).thenReturn(Optional.of(drone));

        var result = service.handle(new GetDroneByIdQuery(1L));

        assertThat(result.toOptional()).containsSame(drone);
    }

    @Test
    void returnsNotFoundWhenTheDroneDoesNotExist() {
        when(droneRepository.findById(99L)).thenReturn(Optional.empty());

        var result = service.handle(new GetDroneByIdQuery(99L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(found -> null, ApplicationError::code)).isEqualTo("DRONE_NOT_FOUND");
    }

    @Test
    void returnsAllTheRecords() {
        var drone = Drone.reconstitute(1L, "DRN-1", "RTX-6030", 36, DroneStatus.AVAILABLE, new NozzleId(3L), 35.0, 4, null, null, null);
        when(droneRepository.findAll()).thenReturn(List.of(drone));

        assertThat(service.handle(new GetAllDronesQuery())).containsExactly(drone);
    }
}