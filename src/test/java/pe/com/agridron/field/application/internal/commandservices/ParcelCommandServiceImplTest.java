package pe.com.agridron.field.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.ChangeParcelStatusCommand;
import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.domain.model.commands.RedefineParcelGeometryCommand;
import pe.com.agridron.field.domain.model.commands.UpdateParcelCommand;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.field.domain.repositories.FarmRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
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
class ParcelCommandServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private CropRepository cropRepository;

    @InjectMocks
    private ParcelCommandServiceImpl service;

    @Test
    void createsParcelWithoutCrop() {
        stubExistingFarm(1L);
        stubSaveWithId(5L);

        var result = service.handle(new CreateParcelCommand(
                1L, "Lote 1", geometry(0.001), null, null));

        assertThat(result.isSuccess()).isTrue();
        var parcel = result.toOptional().orElseThrow();
        assertThat(parcel.getId()).isEqualTo(5L);
        assertThat(parcel.getFarmId()).isEqualTo(1L);
        assertThat(parcel.getStatus()).isEqualTo(ParcelStatus.ACTIVE);
        assertThat(parcel.getCropId()).isNull();
        verify(parcelRepository).existsByFarmIdAndName(1L, "Lote 1");
    }

    @Test
    void createsParcelWithCrop() {
        stubExistingFarm(1L);
        when(cropRepository.existsById(2L)).thenReturn(true);
        stubSaveWithId(5L);

        var result = service.handle(new CreateParcelCommand(
                1L, "Lote 1", geometry(0.001), 2L, PhenologicalStage.VEGETATIVE));

        assertThat(result.isSuccess()).isTrue();
        var parcel = result.toOptional().orElseThrow();
        assertThat(parcel.getCropId()).isEqualTo(2L);
        assertThat(parcel.getPhenologicalStage()).isEqualTo(PhenologicalStage.VEGETATIVE);
    }

    @Test
    void rejectsCreateWhenFarmDoesNotExist() {
        when(farmRepository.findById(1L)).thenReturn(Optional.empty());

        var result = service.handle(new CreateParcelCommand(
                1L, "Lote 1", geometry(0.001), null, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("FARM_NOT_FOUND");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void rejectsCreateWhenCropDoesNotExist() {
        stubExistingFarm(1L);
        when(cropRepository.existsById(2L)).thenReturn(false);

        var result = service.handle(new CreateParcelCommand(
                1L, "Lote 1", geometry(0.001), 2L, PhenologicalStage.VEGETATIVE));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("CROP_NOT_FOUND");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void rejectsDuplicatedNameOnCreate() {
        stubExistingFarm(1L);
        when(parcelRepository.existsByFarmIdAndName(1L, "Lote 1")).thenReturn(true);

        var result = service.handle(new CreateParcelCommand(
                1L, "Lote 1", geometry(0.001), null, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_CONFLICT");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void trimsNameBeforeCheckingDuplicates() {
        stubExistingFarm(1L);
        stubSaveWithId(5L);

        service.handle(new CreateParcelCommand(
                1L, "  Lote 1  ", geometry(0.001), null, null));

        verify(parcelRepository).existsByFarmIdAndName(1L, "Lote 1");
    }

    @Test
    void updatesInformationAndCrop() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.ACTIVE)));
        when(cropRepository.existsById(2L)).thenReturn(true);
        when(parcelRepository.existsByFarmIdAndNameAndIdNot(1L, "Lote 2", 5L)).thenReturn(false);
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateParcelCommand(
                5L, "Lote 2", 2L, PhenologicalStage.FLOWERING));

        assertThat(result.isSuccess()).isTrue();
        var parcel = result.toOptional().orElseThrow();
        assertThat(parcel.getName()).isEqualTo("Lote 2");
        assertThat(parcel.getCropId()).isEqualTo(2L);
        assertThat(parcel.getPhenologicalStage()).isEqualTo(PhenologicalStage.FLOWERING);
    }

    @Test
    void rejectsUpdateWhenParcelDoesNotExist() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.empty());

        var result = service.handle(new UpdateParcelCommand(5L, "Lote 2", null, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_NOT_FOUND");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void rejectsUpdateWhenCropDoesNotExist() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.ACTIVE)));
        when(cropRepository.existsById(2L)).thenReturn(false);

        var result = service.handle(new UpdateParcelCommand(
                5L, "Lote 2", 2L, PhenologicalStage.FLOWERING));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("CROP_NOT_FOUND");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void rejectsDuplicatedNameOnUpdate() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.ACTIVE)));
        when(parcelRepository.existsByFarmIdAndNameAndIdNot(1L, "Lote 2", 5L)).thenReturn(true);

        var result = service.handle(new UpdateParcelCommand(5L, "Lote 2", null, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_CONFLICT");
        verify(parcelRepository, never()).save(any(Parcel.class));
    }

    @Test
    void redefinesParcelGeometry() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.ACTIVE)));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var newGeometry = geometry(0.003);

        var result = service.handle(new RedefineParcelGeometryCommand(5L, newGeometry));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getGeometry().areaHectares())
                .isEqualByComparingTo(newGeometry.areaHectares());
    }

    @Test
    void rejectsRedefiningWhenParcelDoesNotExist() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.empty());

        var result = service.handle(new RedefineParcelGeometryCommand(5L, geometry(0.003)));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_NOT_FOUND");
    }

    @Test
    void deactivatesParcel() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.ACTIVE)));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new ChangeParcelStatusCommand(5L, ParcelStatus.INACTIVE));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(ParcelStatus.INACTIVE);
    }

    @Test
    void activatesParcel() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel(5L, ParcelStatus.INACTIVE)));
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new ChangeParcelStatusCommand(5L, ParcelStatus.ACTIVE));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getStatus()).isEqualTo(ParcelStatus.ACTIVE);
    }

    @Test
    void rejectsNullStatus() {
        var result = service.handle(new ChangeParcelStatusCommand(5L, null));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("VALIDATION_ERROR");
        verify(parcelRepository, never()).findById(5L);
    }

    @Test
    void rejectsStatusChangeWhenParcelDoesNotExist() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.empty());

        var result = service.handle(new ChangeParcelStatusCommand(5L, ParcelStatus.INACTIVE));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_NOT_FOUND");
    }

    private void stubExistingFarm(Long farmId) {
        when(farmRepository.findById(farmId)).thenReturn(Optional.of(
                Farm.reconstitute(farmId, 10L, "Finca El Sol", "Lima", "Lima", Set.of())));
    }

    private void stubSaveWithId(Long id) {
        when(parcelRepository.save(any(Parcel.class))).thenAnswer(invocation -> {
            var parcel = invocation.getArgument(0, Parcel.class);
            return Parcel.reconstitute(
                    id,
                    parcel.getFarmId(),
                    parcel.getCropId(),
                    parcel.getName(),
                    parcel.getGeometry(),
                    parcel.getStatus(),
                    parcel.getPhenologicalStage());
        });
    }

    private Parcel parcel(Long id, ParcelStatus status) {
        return Parcel.reconstitute(
                id, 1L, null, "Lote 1", geometry(0.001), status, null);
    }

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, size));
    }

    private String errorCode(Result<Parcel, ApplicationError> result) {
        return result.fold(parcel -> null, ApplicationError::code);
    }
}
