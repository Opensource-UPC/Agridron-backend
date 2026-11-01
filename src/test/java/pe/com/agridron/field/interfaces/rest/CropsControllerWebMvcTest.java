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
import pe.com.agridron.field.domain.model.queries.PageResult;
import pe.com.agridron.field.domain.model.queries.Pagination;
import pe.com.agridron.field.domain.model.queries.SortDirection;
import pe.com.agridron.field.domain.model.queries.SortOrder;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        when(cropQueryService.handle(new GetAllCropsQuery())).thenReturn(PageResult.unpaged(List.of(
                Crop.reconstitute(1L, "Café", "Catimor"),
                Crop.reconstitute(2L, "Palta", "Hass"))));

        mockMvc.perform(get("/api/v1/crops").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Café"))
                .andExpect(jsonPath("$[1].variety").value("Hass"));
    }

    @Test
    void returnsPagedCrops() throws Exception {
        var query = new GetAllCropsQuery(
                Pagination.of(0, 1),
                new SortOrder("variety", SortDirection.DESC));
        when(cropQueryService.handle(query)).thenReturn(
                new PageResult<>(List.of(Crop.reconstitute(2L, "Palta", "Hass")), 0, 1, 2));

        mockMvc.perform(get("/api/v1/crops")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "variety,desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].variety").value("Hass"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void rejectsUnsupportedCropSortField() throws Exception {
        mockMvc.perform(get("/api/v1/crops")
                        .param("sort", "createdAt")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(cropQueryService, never()).handle(any(GetAllCropsQuery.class));
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
