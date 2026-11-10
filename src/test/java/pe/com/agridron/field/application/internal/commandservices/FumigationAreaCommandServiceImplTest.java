package pe.com.agridron.field.application.internal.commandservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.DelimitFumigationAreaCommand;
import pe.com.agridron.field.domain.model.commands.RedefineFumigationAreaCommand;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FumigationAreaCommandServiceImplTest {

    private static final FieldGeometry PARCEL_GEOMETRY =
            FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.01));

    @Mock
    private FumigationAreaRepository fumigationAreaRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private FumigationAreaCommandServiceImpl service;

    @Test
    void delimitsAreaInsideParcel() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel()));
        stubSaveWithId(9L);

        var result = service.handle(new DelimitFumigationAreaCommand(1L, inside(0.002)));

        assertThat(result.isSuccess()).isTrue();
        var area = result.toOptional().orElseThrow();
        assertThat(area.getId()).isEqualTo(9L);
        assertThat(area.getParcelId()).isEqualTo(1L);
        assertThat(area.getGeometry().areaHectares()).isPositive();
    }

    @Test
    void rejectsDelimitationWhenParcelDoesNotExist() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        var result = service.handle(new DelimitFumigationAreaCommand(1L, inside(0.002)));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_NOT_FOUND");
        verify(fumigationAreaRepository, never()).save(any(FumigationArea.class));
    }

    @Test
    void rejectsAreaOutsideParcel() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel()));

        var result = service.handle(new DelimitFumigationAreaCommand(1L, outside()));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("BUSINESS_RULE_VIOLATION");
        verify(fumigationAreaRepository, never()).save(any(FumigationArea.class));
    }

    @Test
    void redefinesAreaInsideParcel() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.of(area(9L)));
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel()));
        when(fumigationAreaRepository.save(any(FumigationArea.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        var newGeometry = inside(0.003);

        var result = service.handle(new RedefineFumigationAreaCommand(9L, newGeometry));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getGeometry().areaHectares())
                .isEqualByComparingTo(newGeometry.areaHectares());
    }

    @Test
    void rejectsRedefinitionWhenAreaDoesNotExist() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.empty());

        var result = service.handle(new RedefineFumigationAreaCommand(9L, inside(0.003)));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("FUMIGATION_AREA_NOT_FOUND");
        verify(fumigationAreaRepository, never()).save(any(FumigationArea.class));
    }

    @Test
    void rejectsRedefinitionWhenParcelDoesNotExist() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.of(area(9L)));
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        var result = service.handle(new RedefineFumigationAreaCommand(9L, inside(0.003)));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("PARCEL_NOT_FOUND");
    }

    @Test
    void rejectsRedefinitionOutsideParcel() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.of(area(9L)));
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel()));

        var result = service.handle(new RedefineFumigationAreaCommand(9L, outside()));

        assertThat(result.isFailure()).isTrue();
        assertThat(errorCode(result)).isEqualTo("BUSINESS_RULE_VIOLATION");
        verify(fumigationAreaRepository, never()).save(any(FumigationArea.class));
    }

    private Parcel parcel() {
        return Parcel.reconstitute(
                1L, 10L, null, "Lote 1", PARCEL_GEOMETRY, ParcelStatus.ACTIVE, null);
    }

    private FumigationArea area(Long id) {
        return FumigationArea.reconstitute(id, 1L, inside(0.002));
    }

    private void stubSaveWithId(Long id) {
        when(fumigationAreaRepository.save(any(FumigationArea.class))).thenAnswer(invocation -> {
            var area = invocation.getArgument(0, FumigationArea.class);
            return FumigationArea.reconstitute(id, area.getParcelId(), area.getGeometry());
        });
    }

    private static FieldGeometry inside(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.041, -12.045, size));
    }

    private static FieldGeometry outside() {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.030, -12.030, 0.002));
    }

    private String errorCode(Result<FumigationArea, ApplicationError> result) {
        return result.fold(area -> null, ApplicationError::code);
    }
}
