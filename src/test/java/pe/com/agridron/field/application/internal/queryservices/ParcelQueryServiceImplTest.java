package pe.com.agridron.field.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.queries.GetParcelByIdQuery;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.repositories.ParcelRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParcelQueryServiceImplTest {

    @Mock
    private ParcelRepository parcelRepository;

    @InjectMocks
    private ParcelQueryServiceImpl service;

    @Test
    void returnsParcelById() {
        var parcel = parcel(5L, "Lote 1");
        when(parcelRepository.findById(5L)).thenReturn(Optional.of(parcel));

        var result = service.handle(new GetParcelByIdQuery(5L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getName()).isEqualTo("Lote 1");
    }

    @Test
    void returnsNotFoundWhenParcelDoesNotExist() {
        when(parcelRepository.findById(5L)).thenReturn(Optional.empty());

        var result = service.handle(new GetParcelByIdQuery(5L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(parcel -> null, ApplicationError::code)).isEqualTo("PARCEL_NOT_FOUND");
    }

    @Test
    void delegatesSearchToRepository() {
        var criteria = new ParcelSearchCriteria(1L, ParcelStatus.ACTIVE, 2L, null, null);
        when(parcelRepository.search(criteria)).thenReturn(List.of(parcel(5L, "Alfa")));

        var result = service.handle(criteria);

        assertThat(result).extracting(Parcel::getName).containsExactly("Alfa");
        verify(parcelRepository).search(criteria);
    }

    private Parcel parcel(Long id, String name) {
        return Parcel.reconstitute(
                id, 1L, null, name, geometry(), ParcelStatus.ACTIVE, null);
    }

    private static FieldGeometry geometry() {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.001));
    }
}
