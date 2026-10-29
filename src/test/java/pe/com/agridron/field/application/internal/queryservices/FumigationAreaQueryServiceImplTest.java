package pe.com.agridron.field.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreaByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreasByParcelQuery;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.repositories.FumigationAreaRepository;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FumigationAreaQueryServiceImplTest {

    private static final FieldGeometry PARCEL_GEOMETRY =
            FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.01));

    @Mock
    private FumigationAreaRepository fumigationAreaRepository;

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private FumigationAreaQueryServiceImpl service;

    @Test
    void returnsAreaById() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.of(area(9L)));

        var result = service.handle(new GetFumigationAreaByIdQuery(9L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getParcelId()).isEqualTo(1L);
    }

    @Test
    void returnsNotFoundWhenAreaDoesNotExist() {
        when(fumigationAreaRepository.findById(9L)).thenReturn(Optional.empty());

        var result = service.handle(new GetFumigationAreaByIdQuery(9L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(area -> null, ApplicationError::code))
                .isEqualTo("FUMIGATION_AREA_NOT_FOUND");
    }

    @Test
    void listsAreasOfParcel() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.of(parcel()));
        when(fumigationAreaRepository.findByParcelId(1L)).thenReturn(List.of(area(9L), area(10L)));

        var result = service.handle(new GetFumigationAreasByParcelQuery(1L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow())
                .extracting(FumigationArea::getId)
                .containsExactly(9L, 10L);
        verify(fumigationAreaRepository).findByParcelId(1L);
    }

    @Test
    void returnsNotFoundWhenParcelDoesNotExist() {
        when(parcelRepository.findById(1L)).thenReturn(Optional.empty());

        var result = service.handle(new GetFumigationAreasByParcelQuery(1L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(areas -> null, ApplicationError::code)).isEqualTo("PARCEL_NOT_FOUND");
    }

    private Parcel parcel() {
        return Parcel.reconstitute(
                1L, 10L, null, "Lote 1", PARCEL_GEOMETRY, ParcelStatus.ACTIVE, null);
    }

    private FumigationArea area(Long id) {
        var geometry = FieldGeometry.from(TestGeometries.wgs84Square(-77.041, -12.045, 0.002));
        return FumigationArea.reconstitute(id, 1L, geometry);
    }
}
