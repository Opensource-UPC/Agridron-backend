package pe.com.agridron.field.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.agridron.field.application.queryservices.CropQueryService;
import pe.com.agridron.field.domain.model.aggregates.Crop;
import pe.com.agridron.field.domain.model.queries.GetAllCropsQuery;
import pe.com.agridron.field.domain.model.queries.GetCropByIdQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CropsController.class)
class CropsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CropQueryService cropQueryService;

    @Test
    void listsEveryCrop() throws Exception {
        when(cropQueryService.handle(new GetAllCropsQuery())).thenReturn(List.of(
                Crop.reconstitute(1L, "Café", "Catimor"),
                Crop.reconstitute(2L, "Palta", "Hass")));

        mockMvc.perform(get("/api/v1/crops").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Café"))
                .andExpect(jsonPath("$[1].variety").value("Hass"));
    }

    @Test
    void returnsCropById() throws Exception {
        when(cropQueryService.handle(new GetCropByIdQuery(2L)))
                .thenReturn(Result.success(Crop.reconstitute(2L, "Palta", "Hass")));

        mockMvc.perform(get("/api/v1/crops/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Palta"))
                .andExpect(jsonPath("$.variety").value("Hass"));
    }

    @Test
    void returnsNotFoundWhenCropDoesNotExist() throws Exception {
        when(cropQueryService.handle(new GetCropByIdQuery(99L)))
                .thenReturn(Result.failure(ApplicationError.notFound("Crop", "99")));

        mockMvc.perform(get("/api/v1/crops/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CROP_NOT_FOUND"));
    }
}
