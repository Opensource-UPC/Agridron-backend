package pe.com.agridron.field.application.internal.queryservices;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
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
        when(cropRepository.findAll()).thenReturn(List.of(
                Crop.reconstitute(1L, "Café", "Catimor"),
                Crop.reconstitute(2L, "Palta", "Hass")));

        var result = service.handle(new GetAllCropsQuery());

        assertThat(result).extracting(Crop::getName).containsExactly("Café", "Palta");
        verify(cropRepository).findAll();
    }
}
