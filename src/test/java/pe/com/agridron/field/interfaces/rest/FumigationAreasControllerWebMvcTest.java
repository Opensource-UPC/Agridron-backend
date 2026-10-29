package pe.com.agridron.field.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.agridron.field.application.commandservices.FumigationAreaCommandService;
import pe.com.agridron.field.application.queryservices.FumigationAreaQueryService;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.FumigationArea;
import pe.com.agridron.field.domain.model.commands.DelimitFumigationAreaCommand;
import pe.com.agridron.field.domain.model.commands.RedefineFumigationAreaCommand;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreaByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFumigationAreasByParcelQuery;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FumigationAreasController.class)
class FumigationAreasControllerWebMvcTest {

    private static final String SQUARE = """
            {"type":"Polygon","coordinates":[[[-77.041,-12.045],[-77.040,-12.045],\
            [-77.040,-12.044],[-77.041,-12.044],[-77.041,-12.045]]]}
            """;

    private static final String OPEN_SQUARE = """
            {"type":"Polygon","coordinates":[[[-77.041,-12.045],[-77.040,-12.045],\
            [-77.040,-12.044],[-77.041,-12.044],[-77.041,-12.0455]]]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FumigationAreaCommandService fumigationAreaCommandService;

    @MockitoBean
    private FumigationAreaQueryService fumigationAreaQueryService;

    @Test
    void delimitsFumigationArea() throws Exception {
        when(fumigationAreaCommandService.handle(any(DelimitFumigationAreaCommand.class)))
                .thenReturn(Result.success(area(1L)));

        mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":1,"geometry":%s}
                                """.formatted(SQUARE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.parcelId").value(1))
                .andExpect(jsonPath("$.geometry.type").value("Polygon"))
                .andExpect(jsonPath("$.areaHectares").isNumber());

        var captor = ArgumentCaptor.forClass(DelimitFumigationAreaCommand.class);
        verify(fumigationAreaCommandService).handle(captor.capture());
        assertThat(captor.getValue().parcelId()).isEqualTo(1L);
        assertThat(captor.getValue().geometry().areaHectares()).isPositive();
    }

    @Test
    void rejectsAreaOutsideParcel() throws Exception {
        when(fumigationAreaCommandService.handle(any(DelimitFumigationAreaCommand.class)))
                .thenReturn(Result.failure(ApplicationError.businessRuleViolation(
                        "fumigation-area-containment",
                        "fumigation area must be fully contained in its parcel")));

        mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":1,"geometry":%s}
                                """.formatted(SQUARE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    void returnsNotFoundWhenParcelDoesNotExist() throws Exception {
        when(fumigationAreaCommandService.handle(any(DelimitFumigationAreaCommand.class)))
                .thenReturn(Result.failure(ApplicationError.notFound("Parcel", "99")));

        mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":99,"geometry":%s}
                                """.formatted(SQUARE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PARCEL_NOT_FOUND"));
    }

    @Test
    void rejectsCreateWithInvalidGeometry() throws Exception {
        mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":1,"geometry":%s}
                                """.formatted(OPEN_SQUARE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"));

        verify(fumigationAreaCommandService, never()).handle(any(DelimitFumigationAreaCommand.class));
    }

    @Test
    void listsFumigationAreasByParcel() throws Exception {
        when(fumigationAreaQueryService.handle(new GetFumigationAreasByParcelQuery(1L)))
                .thenReturn(Result.success(List.of(area(1L), area(2L))));

        mockMvc.perform(get("/api/v1/fumigation-areas")
                        .param("parcelId", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(fumigationAreaQueryService).handle(new GetFumigationAreasByParcelQuery(1L));
    }

    @Test
    void rejectsListWithoutParcelId() throws Exception {
        mockMvc.perform(get("/api/v1/fumigation-areas").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(fumigationAreaQueryService, never())
                .handle(any(GetFumigationAreasByParcelQuery.class));
    }

    @Test
    void returnsNotFoundWhenListingAreasOfUnknownParcel() throws Exception {
        when(fumigationAreaQueryService.handle(new GetFumigationAreasByParcelQuery(99L)))
                .thenReturn(Result.failure(ApplicationError.notFound("Parcel", "99")));

        mockMvc.perform(get("/api/v1/fumigation-areas")
                        .param("parcelId", "99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PARCEL_NOT_FOUND"));
    }

    @Test
    void returnsFumigationAreaById() throws Exception {
        when(fumigationAreaQueryService.handle(new GetFumigationAreaByIdQuery(1L)))
                .thenReturn(Result.success(area(1L)));

        mockMvc.perform(get("/api/v1/fumigation-areas/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.parcelId").value(1));
    }

    @Test
    void returnsNotFoundWhenAreaDoesNotExist() throws Exception {
        when(fumigationAreaQueryService.handle(new GetFumigationAreaByIdQuery(99L)))
                .thenReturn(Result.failure(
                        ApplicationError.notFound("Fumigation Area", "99")));

        mockMvc.perform(get("/api/v1/fumigation-areas/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FUMIGATION_AREA_NOT_FOUND"));
    }

    @Test
    void redefinesFumigationArea() throws Exception {
        when(fumigationAreaCommandService.handle(any(RedefineFumigationAreaCommand.class)))
                .thenReturn(Result.success(area(1L)));

        mockMvc.perform(put("/api/v1/fumigation-areas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SQUARE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        var captor = ArgumentCaptor.forClass(RedefineFumigationAreaCommand.class);
        verify(fumigationAreaCommandService).handle(captor.capture());
        assertThat(captor.getValue().fumigationAreaId()).isEqualTo(1L);
        assertThat(captor.getValue().geometry().areaHectares()).isPositive();
    }

    @Test
    void rejectsRedefiningWithInvalidGeometry() throws Exception {
        mockMvc.perform(put("/api/v1/fumigation-areas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OPEN_SQUARE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"));

        verify(fumigationAreaCommandService, never()).handle(any(RedefineFumigationAreaCommand.class));
    }

    private static FumigationArea area(Long id) {
        return FumigationArea.reconstitute(id, 1L, geometry());
    }

    private static FieldGeometry geometry() {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.041, -12.045, 0.002));
    }
}
