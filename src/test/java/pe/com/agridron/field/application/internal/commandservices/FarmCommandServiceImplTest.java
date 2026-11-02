package pe.com.agridron.field.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.commands.AssignFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.domain.model.commands.RemoveFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.UpdateFarmCommand;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmCommandServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private FarmCommandServiceImpl service;

    @Test
    void createsFarmWhenNameIsAvailable() {
        when(farmRepository.existsByOwnerIdAndName(10L, "Finca El Sol")).thenReturn(false);
        stubSaveWithId(7L);

        var result = service.handle(new CreateFarmCommand(10L, "Finca El Sol", "Lima", "Lima"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getId()).isEqualTo(7L);
        assertThat(result.toOptional().orElseThrow().getName()).isEqualTo("Finca El Sol");
        verify(farmRepository).save(any(Farm.class));
    }

    @Test
    void rejectsDuplicatedNameOnCreate() {
        when(farmRepository.existsByOwnerIdAndName(10L, "Finca El Sol")).thenReturn(true);

        var result = service.handle(new CreateFarmCommand(10L, "Finca El Sol", "Lima", "Lima"));

        assertThat(result.isFailure()).isTrue();
        assertThat(resultErrorCode(result)).isEqualTo("FARM_CONFLICT");
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void trimsNameBeforeCheckingDuplicates() {
        when(farmRepository.existsByOwnerIdAndName(10L, "Finca El Sol")).thenReturn(false);
        stubSaveWithId(7L);

        service.handle(new CreateFarmCommand(10L, "  Finca El Sol  ", "Lima", "Lima"));

        verify(farmRepository).existsByOwnerIdAndName(10L, "Finca El Sol");
    }

    @Test
    void updatesFarmWhenNameIsAvailable() {
        when(farmRepository.findById(7L)).thenReturn(Optional.of(farm(7L, Set.of())));
        when(farmRepository.existsByOwnerIdAndNameAndIdNot(10L, "Nueva Finca", 7L)).thenReturn(false);
        when(farmRepository.save(any(Farm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateFarmCommand(7L, "Nueva Finca", "Ica", "Ica"));

        assertThat(result.isSuccess()).isTrue();
        var updated = result.toOptional().orElseThrow();
        assertThat(updated.getName()).isEqualTo("Nueva Finca");
        assertThat(updated.getLocation()).isEqualTo("Ica");
        assertThat(updated.getDepartment()).isEqualTo("Ica");
    }

    @Test
    void rejectsUpdateWhenFarmDoesNotExist() {
        when(farmRepository.findById(7L)).thenReturn(Optional.empty());

        var result = service.handle(new UpdateFarmCommand(7L, "Nueva Finca", "Ica", "Ica"));

        assertThat(result.isFailure()).isTrue();
        assertThat(resultErrorCode(result)).isEqualTo("FARM_NOT_FOUND");
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void rejectsDuplicatedNameOnUpdate() {
        when(farmRepository.findById(7L)).thenReturn(Optional.of(farm(7L, Set.of())));
        when(farmRepository.existsByOwnerIdAndNameAndIdNot(10L, "Nueva Finca", 7L)).thenReturn(true);

        var result = service.handle(new UpdateFarmCommand(7L, "Nueva Finca", "Ica", "Ica"));

        assertThat(result.isFailure()).isTrue();
        assertThat(resultErrorCode(result)).isEqualTo("FARM_CONFLICT");
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void assignsSupervisorToFarm() {
        when(farmRepository.findById(7L)).thenReturn(Optional.of(farm(7L, Set.of())));
        when(farmRepository.save(any(Farm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new AssignFarmSupervisorCommand(7L, 20L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getSupervisorIds()).containsExactly(20L);
    }

    @Test
    void rejectsAssignmentWhenFarmDoesNotExist() {
        when(farmRepository.findById(7L)).thenReturn(Optional.empty());

        var result = service.handle(new AssignFarmSupervisorCommand(7L, 20L));

        assertThat(result.isFailure()).isTrue();
        assertThat(resultErrorCode(result)).isEqualTo("FARM_NOT_FOUND");
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void removesSupervisorFromFarm() {
        when(farmRepository.findById(7L)).thenReturn(Optional.of(farm(7L, Set.of(20L))));
        when(farmRepository.save(any(Farm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new RemoveFarmSupervisorCommand(7L, 20L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getSupervisorIds()).isEmpty();
    }

    @Test
    void rejectsRemovalWhenFarmDoesNotExist() {
        when(farmRepository.findById(7L)).thenReturn(Optional.empty());

        var result = service.handle(new RemoveFarmSupervisorCommand(7L, 20L));

        assertThat(result.isFailure()).isTrue();
        assertThat(resultErrorCode(result)).isEqualTo("FARM_NOT_FOUND");
        verify(farmRepository, never()).save(any(Farm.class));
    }

    private Farm farm(Long id, Set<Long> supervisorIds) {
        return Farm.reconstitute(id, 10L, "Finca El Sol", "Lima", "Lima", supervisorIds);
    }

    private void stubSaveWithId(Long id) {
        when(farmRepository.save(any(Farm.class))).thenAnswer(invocation -> {
            var farm = invocation.getArgument(0, Farm.class);
            return Farm.reconstitute(
                    id,
                    farm.getOwnerId(),
                    farm.getName(),
                    farm.getLocation(),
                    farm.getDepartment(),
                    farm.getSupervisorIds());
        });
    }

    private String resultErrorCode(Result<Farm, ApplicationError> result) {
        return result.fold(farm -> null, ApplicationError::code);
    }
}
