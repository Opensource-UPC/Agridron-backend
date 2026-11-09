package pe.com.agridron.inventory.application.internal.commandservices;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.inventory.domain.model.aggregates.Drone;
import pe.com.agridron.inventory.domain.model.aggregates.Nozzle;
import pe.com.agridron.inventory.domain.model.commands.CreateDroneCommand;
import pe.com.agridron.inventory.domain.model.valueobjects.DroneStatus;
import pe.com.agridron.inventory.domain.model.valueobjects.NozzleStock;
import pe.com.agridron.inventory.domain.repositories.DroneRepository;
import pe.com.agridron.inventory.domain.repositories.NozzleRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DroneCommandServiceImplTest {

    @Mock
    private DroneRepository droneRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private DroneCommandServiceImpl service;

    @Test
    void registersAnAvailableDroneAndTakesOneNozzleUnit() {
        when(droneRepository.existsBySerialNumber("DRN-2026-0101")).thenReturn(false);
        when(nozzleRepository.findById(3L)).thenReturn(Optional.of(nozzle(3L, new NozzleStock(5, 1, 0))));
        stubDroneSaveWithId(10L);

        var result = service.handle(command(3L, 36));

        assertThat(result.isSuccess()).isTrue();
        var drone = result.toOptional().orElseThrow();
        assertThat(drone.getId()).isEqualTo(10L);
        assertThat(drone.getStatus()).isEqualTo(DroneStatus.AVAILABLE);
        assertThat(drone.getFlightHours()).isZero();
        assertThat(drone.getNozzleId().id()).isEqualTo(3L);

        var savedNozzle = ArgumentCaptor.forClass(Nozzle.class);
        verify(nozzleRepository).save(savedNozzle.capture());
        assertThat(savedNozzle.getValue().getStock()).isEqualTo(new NozzleStock(4, 1, 0));
    }

    @Test
    void usesAWornUnitWhenThereAreNoGoodUnits() {
        when(droneRepository.existsBySerialNumber("DRN-2026-0101")).thenReturn(false);
        when(nozzleRepository.findById(3L)).thenReturn(Optional.of(nozzle(3L, new NozzleStock(0, 2, 1))));
        stubDroneSaveWithId(10L);

        var result = service.handle(command(3L, 36));

        assertThat(result.isSuccess()).isTrue();
        var savedNozzle = ArgumentCaptor.forClass(Nozzle.class);
        verify(nozzleRepository).save(savedNozzle.capture());
        assertThat(savedNozzle.getValue().getStock()).isEqualTo(new NozzleStock(0, 1, 1));
    }

    @Test
    void rejectsADuplicatedSerialNumber() {
        when(droneRepository.existsBySerialNumber("DRN-2026-0101")).thenReturn(true);

        var result = service.handle(command(3L, 36));

        assertThat(errorCode(result)).isEqualTo("DRONE_CONFLICT");
        verifyNothingWasSaved();
    }

    @Test
    void rejectsANozzleThatDoesNotExist() {
        when(droneRepository.existsBySerialNumber("DRN-2026-0101")).thenReturn(false);
        when(nozzleRepository.findById(99L)).thenReturn(Optional.empty());

        var result = service.handle(command(99L, 36));

        assertThat(errorCode(result)).isEqualTo("NOZZLE_NOT_FOUND");
        verifyNothingWasSaved();
    }

    @Test
    void rejectsANozzleWithoutInstallableUnits() {
        when(droneRepository.existsBySerialNumber("DRN-2026-0101")).thenReturn(false);
        when(nozzleRepository.findById(3L)).thenReturn(Optional.of(nozzle(3L, new NozzleStock(0, 0, 4))));

        var result = service.handle(command(3L, 36));

        assertThat(errorCode(result)).isEqualTo("INSUFFICIENT_STOCK");
        verifyNothingWasSaved();
    }

    @Test
    void rejectsInvalidDroneData() {
        var result = service.handle(command(3L, 150));

        assertThat(errorCode(result)).isEqualTo("VALIDATION_ERROR");
        verifyNothingWasSaved();
    }

    private static CreateDroneCommand command(Long nozzleId, int batteryLevel) {
        return new CreateDroneCommand("DRN-2026-0101", "RTX-6030", batteryLevel, nozzleId, 35.0,
                null, null, null);
    }

    private static Nozzle nozzle(Long id, NozzleStock stock) {
        return Nozzle.reconstitute(id, "flat-fan", 5.0, stock);
    }

    private void stubDroneSaveWithId(Long id) {
        when(droneRepository.save(any(Drone.class))).thenAnswer(invocation -> {
            var d = invocation.getArgument(0, Drone.class);
            return Drone.reconstitute(id, d.getSerialNumber(), d.getModel(), d.getBatteryLevel(),
                    d.getStatus(), d.getNozzleId(), d.getTankCapacityLiters(), d.getFlightHours(),
                    d.getLastMaintenanceDate(), d.getNextMaintenanceDate(), d.getUrlimg());
        });
    }

    private void verifyNothingWasSaved() {
        verify(droneRepository, never()).save(any(Drone.class));
        verify(nozzleRepository, never()).save(any(Nozzle.class));
    }

    private static String errorCode(Result<Drone, ApplicationError> result) {
        assertThat(result.isFailure()).isTrue();
        return result.fold(drone -> null, ApplicationError::code);
    }
}