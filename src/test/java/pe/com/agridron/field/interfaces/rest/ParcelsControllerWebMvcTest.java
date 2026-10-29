package pe.com.agridron.field.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.agridron.field.application.commandservices.ParcelCommandService;
import pe.com.agridron.field.application.queryservices.ParcelQueryService;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.ChangeParcelStatusCommand;
import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.domain.model.commands.RedefineParcelGeometryCommand;
import pe.com.agridron.field.domain.model.commands.UpdateParcelCommand;
import pe.com.agridron.field.domain.model.queries.GetParcelByIdQuery;
import pe.com.agridron.field.domain.model.queries.ParcelSearchCriteria;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.model.valueobjects.ParcelStatus;
import pe.com.agridron.field.domain.model.valueobjects.PhenologicalStage;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParcelsController.class)
class ParcelsControllerWebMvcTest {

    private static final String SQUARE = """
            {"type":"Polygon","coordinates":[[[-77.042,-12.046],[-77.041,-12.046],\
            [-77.041,-12.045],[-77.042,-12.045],[-77.042,-12.046]]]}
            """;

    private static final String OPEN_SQUARE = """
            {"type":"Polygon","coordinates":[[[-77.042,-12.046],[-77.041,-12.046],\
            [-77.041,-12.045],[-77.042,-12.045],[-77.042,-12.0465]]]}
            """;

    private static final String CREATE_JSON = """
            {"farmId":1,"name":"Lote 1","geometry":%s,"cropId":2,"phenologicalStage":"VEGETATIVE"}
            """.formatted(SQUARE);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParcelCommandService parcelCommandService;

    @MockitoBean
    private ParcelQueryService parcelQueryService;

    @Test
    void createsParcel() throws Exception {
        when(parcelCommandService.handle(any(CreateParcelCommand.class)))
                .thenReturn(Result.success(parcel(5L, ParcelStatus.ACTIVE)));

        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.farmId").value(1))
                .andExpect(jsonPath("$.name").value("Lote 1"))
                .andExpect(jsonPath("$.geometry.type").value("Polygon"))
                .andExpect(jsonPath("$.geometry.coordinates", hasSize(1)))
                .andExpect(jsonPath("$.areaHectares").isNumber())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.phenologicalStage").value("VEGETATIVE"));

        var captor = ArgumentCaptor.forClass(CreateParcelCommand.class);
        verify(parcelCommandService).handle(captor.capture());
        var command = captor.getValue();
        assertThat(command.farmId()).isEqualTo(1L);
        assertThat(command.name()).isEqualTo("Lote 1");
        assertThat(command.cropId()).isEqualTo(2L);
        assertThat(command.phenologicalStage()).isEqualTo(PhenologicalStage.VEGETATIVE);
        assertThat(command.geometry().polygon().getSRID()).isEqualTo(4326);
        assertThat(command.geometry().areaHectares()).isPositive();
    }

    @Test
    void rejectsCreateWithInvalidGeometry() throws Exception {
        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"farmId":1,"name":"Lote 1","geometry":%s}
                                """.formatted(OPEN_SQUARE))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"));

        verify(parcelCommandService, never()).handle(any(CreateParcelCommand.class));
    }

    @Test
    void returnsConflictWhenNameIsDuplicated() throws Exception {
        when(parcelCommandService.handle(any(CreateParcelCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("Parcel", "duplicated name")));

        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PARCEL_CONFLICT"));
    }

    @Test
    void returnsParcelById() throws Exception {
        when(parcelQueryService.handle(new GetParcelByIdQuery(5L)))
                .thenReturn(Result.success(parcel(5L, ParcelStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/parcels/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void returnsNotFoundWhenParcelDoesNotExist() throws Exception {
        when(parcelQueryService.handle(new GetParcelByIdQuery(99L)))
                .thenReturn(Result.failure(ApplicationError.notFound("Parcel", "99")));

        mockMvc.perform(get("/api/v1/parcels/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PARCEL_NOT_FOUND"));
    }

    @Test
    void searchesParcelsWithEveryFilter() throws Exception {
        when(parcelQueryService.handle(argThat((ParcelSearchCriteria criteria) ->
                criteria.farmId().equals(1L)
                        && criteria.status() == ParcelStatus.ACTIVE
                        && criteria.cropId().equals(2L)
                        && criteria.minAreaHectares().compareTo(new BigDecimal("0.5")) == 0
                        && criteria.maxAreaHectares().compareTo(new BigDecimal("10")) == 0)))
                .thenReturn(List.of(parcel(5L, ParcelStatus.ACTIVE)));

        mockMvc.perform(get("/api/v1/parcels")
                        .param("farmId", "1")
                        .param("status", "ACTIVE")
                        .param("cropId", "2")
                        .param("minAreaHectares", "0.5")
                        .param("maxAreaHectares", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(5));
    }

    @Test
    void searchesEveryParcelWithoutFilters() throws Exception {
        when(parcelQueryService.handle(argThat((ParcelSearchCriteria criteria) ->
                criteria.farmId() == null
                        && criteria.status() == null
                        && criteria.cropId() == null
                        && criteria.minAreaHectares() == null
                        && criteria.maxAreaHectares() == null)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/parcels").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void rejectsInvalidAreaRange() throws Exception {
        mockMvc.perform(get("/api/v1/parcels")
                        .param("minAreaHectares", "10")
                        .param("maxAreaHectares", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(parcelQueryService, never()).handle(any(ParcelSearchCriteria.class));
    }

    @Test
    void updatesParcel() throws Exception {
        when(parcelCommandService.handle(any(UpdateParcelCommand.class)))
                .thenReturn(Result.success(parcel(5L, ParcelStatus.ACTIVE)));

        mockMvc.perform(put("/api/v1/parcels/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lote 2","cropId":2,"phenologicalStage":"FLOWERING"}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(parcelCommandService).handle(
                new UpdateParcelCommand(5L, "Lote 2", 2L, PhenologicalStage.FLOWERING));
    }

    @Test
    void redefinesParcelGeometry() throws Exception {
        when(parcelCommandService.handle(any(RedefineParcelGeometryCommand.class)))
                .thenReturn(Result.success(parcel(5L, ParcelStatus.ACTIVE)));

        mockMvc.perform(put("/api/v1/parcels/5/geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SQUARE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        var captor = ArgumentCaptor.forClass(RedefineParcelGeometryCommand.class);
        verify(parcelCommandService).handle(captor.capture());
        assertThat(captor.getValue().parcelId()).isEqualTo(5L);
        assertThat(captor.getValue().geometry().areaHectares()).isPositive();
    }

    @Test
    void rejectsRedefiningWithInvalidGeometry() throws Exception {
        mockMvc.perform(put("/api/v1/parcels/5/geometry")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(OPEN_SQUARE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"));

        verify(parcelCommandService, never()).handle(any(RedefineParcelGeometryCommand.class));
    }

    @Test
    void changesParcelStatus() throws Exception {
        when(parcelCommandService.handle(any(ChangeParcelStatusCommand.class)))
                .thenReturn(Result.success(parcel(5L, ParcelStatus.INACTIVE)));

        mockMvc.perform(patch("/api/v1/parcels/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(parcelCommandService).handle(
                new ChangeParcelStatusCommand(5L, ParcelStatus.INACTIVE));
    }

    @Test
    void rejectsUnknownStatus() throws Exception {
        mockMvc.perform(patch("/api/v1/parcels/5/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARCHIVED\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(parcelCommandService, never()).handle(any(ChangeParcelStatusCommand.class));
    }

    private static Parcel parcel(Long id, ParcelStatus status) {
        return Parcel.reconstitute(
                id, 1L, 2L, "Lote 1", geometry(), status, PhenologicalStage.VEGETATIVE);
    }

    private static FieldGeometry geometry() {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, 0.001));
    }
}
