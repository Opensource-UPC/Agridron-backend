package pe.com.agridron.field.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.field.domain.repositories.CropRepository;
import pe.com.agridron.shared.application.result.ApplicationError;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CropQueryServiceImplTest {

    @Mock
    private CropRepository cropRepository;

    @InjectMocks
    private CropQueryServiceImpl service;

    @Test
    void returnsCropById() {
        when(cropRepository.findById(2L)).thenReturn(Optional.of(Crop.reconstitute(2L, "Palta", "Hass")));

        var result = service.handle(new GetCropByIdQuery(2L));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.toOptional().orElseThrow().getVariety()).isEqualTo("Hass");
    }

    @Test
    void returnsNotFoundWhenCropDoesNotExist() {
        when(cropRepository.findById(2L)).thenReturn(Optional.empty());

        var result = service.handle(new GetCropByIdQuery(2L));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.fold(crop -> null, ApplicationError::code)).isEqualTo("CROP_NOT_FOUND");
    }

    @Test
    void listsEveryCrop() {
        when(cropRepository.findAll(Pagination.unpaged(), null)).thenReturn(PageResult.unpaged(List.of(
                Crop.reconstitute(1L, "Café", "Catimor"),
                Crop.reconstitute(2L, "Palta", "Hass"))));

        var result = service.handle(new GetAllCropsQuery());

        assertThat(result.content()).extracting(Crop::getName).containsExactly("Café", "Palta");
        verify(cropRepository).findAll(Pagination.unpaged(), null);
    }

    @Test
    void delegatesPaginationAndOrdering() {
        var pagination = Pagination.of(1, 1);
        var sortOrder = new SortOrder("variety", SortDirection.DESC);
        when(cropRepository.findAll(pagination, sortOrder))
                .thenReturn(new PageResult<>(
                        List.of(Crop.reconstitute(2L, "Palta", "Hass")), 1, 1, 2));

        var result = service.handle(new GetAllCropsQuery(pagination, sortOrder));

        assertThat(result.content()).extracting(Crop::getName).containsExactly("Palta");
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(2);
    }
}
