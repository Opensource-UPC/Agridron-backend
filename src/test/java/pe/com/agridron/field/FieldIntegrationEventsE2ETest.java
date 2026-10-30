package pe.com.agridron.field;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.com.agridron.field.application.commandservices.FarmCommandService;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.interfaces.events.FarmRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorAssignedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FarmSupervisorRemovedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.FumigationAreaDelimitedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelCropInformationRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelGeometryUpdatedIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelRegisteredIntegrationEvent;
import pe.com.agridron.field.interfaces.events.ParcelStatusChangedIntegrationEvent;
import pe.com.agridron.shared.domain.events.IntegrationEvent;
import pe.com.agridron.support.AbstractPostgisIntegrationTest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that Field Management publishes integration events only after the transaction
 * commits.
 *
 * <p>These tests are deliberately non-transactional: {@code AFTER_COMMIT} listeners never
 * run inside a test-managed transaction. The database is cleaned before each test and the
 * seeded crop catalog is left untouched.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@RecordApplicationEvents
class FieldIntegrationEventsE2ETest extends AbstractPostgisIntegrationTest {

    private static final long OWNER_ID = 3001L;
    private static final long SUPERVISOR_ID = 4001L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FarmCommandService farmCommandService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void cleanFieldTables() {
        truncateFieldTables();
    }

    @AfterEach
    void cleanFieldTablesAfterTest() {
        truncateFieldTables();
    }

    private void truncateFieldTables() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE fumigation_area, parcel, farm_supervisor, farm RESTART IDENTITY CASCADE");
    }

    @Test
    void publishesEveryFieldIntegrationEventAfterCommit(ApplicationEvents applicationEvents)
            throws Exception {
        long cropId = firstCropId();

        long farmId = readId(mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmJson("Finca Eventos"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(post("/api/v1/farms/{farmId}/supervisors", farmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisorId\":%d}".formatted(SUPERVISOR_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/farms/{farmId}/supervisors/{supervisorId}",
                        farmId, SUPERVISOR_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        long parcelId = readId(mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parcelJson(
                                farmId, "Lote Eventos", cropId, polygonJson(-77.050, -12.050, 0.01)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn());

        mockMvc.perform(put("/api/v1/parcels/{parcelId}", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lote Eventos Actualizado","cropId":%d,"phenologicalStage":"FLOWERING"}
                                """.formatted(cropId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/parcels/{parcelId}/geometry", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(polygonJson(-77.060, -12.060, 0.03))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/v1/parcels/{parcelId}/status", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        long areaId = readId(mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":%d,"geometry":%s}
                                """.formatted(parcelId, polygonJson(-77.045, -12.045, 0.002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn());

        assertThat(applicationEvents.stream(IntegrationEvent.class)
                .map(IntegrationEvent::eventType)
                .toList())
                .containsExactly(
                        "field.farm.registered",
                        "field.farm.supervisor-assigned",
                        "field.farm.supervisor-removed",
                        "field.parcel.registered",
                        "field.parcel.crop-information-registered",
                        "field.parcel.crop-information-registered",
                        "field.parcel.geometry-updated",
                        "field.parcel.status-changed",
                        "field.fumigation-area.delimited");

        assertThat(applicationEvents.stream(IntegrationEvent.class)
                .map(IntegrationEvent::eventId))
                .doesNotHaveDuplicates();

        assertThat(applicationEvents.stream(FarmRegisteredIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.ownerId()).isEqualTo(OWNER_ID);
                    assertThat(event.name()).isEqualTo("Finca Eventos");
                });

        assertThat(applicationEvents.stream(FarmSupervisorAssignedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.supervisorId()).isEqualTo(SUPERVISOR_ID);
                });

        assertThat(applicationEvents.stream(FarmSupervisorRemovedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.supervisorId()).isEqualTo(SUPERVISOR_ID);
                });

        assertThat(applicationEvents.stream(ParcelRegisteredIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.areaHectares()).isPositive();
                });

        var cropInformationEvents = applicationEvents
                .stream(ParcelCropInformationRegisteredIntegrationEvent.class)
                .toList();
        assertThat(cropInformationEvents).hasSize(2);
        assertThat(cropInformationEvents)
                .allSatisfy(event -> {
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.cropId()).isEqualTo(cropId);
                });
        assertThat(cropInformationEvents)
                .extracting(ParcelCropInformationRegisteredIntegrationEvent::phenologicalStage)
                .containsExactly("VEGETATIVE", "FLOWERING");

        assertThat(applicationEvents.stream(ParcelGeometryUpdatedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.areaHectares()).isPositive();
                });

        assertThat(applicationEvents.stream(ParcelStatusChangedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.farmId()).isEqualTo(farmId);
                    assertThat(event.status()).isEqualTo("INACTIVE");
                });

        assertThat(applicationEvents.stream(FumigationAreaDelimitedIntegrationEvent.class))
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.fumigationAreaId()).isEqualTo(areaId);
                    assertThat(event.parcelId()).isEqualTo(parcelId);
                    assertThat(event.areaHectares()).isPositive();
                });
    }

    @Test
    void doesNotPublishIntegrationEventsWhenTheTransactionRollsBack(
            ApplicationEvents applicationEvents) {
        var transactionTemplate = new TransactionTemplate(transactionManager);

        transactionTemplate.executeWithoutResult(status -> {
            var result = farmCommandService.handle(
                    new CreateFarmCommand(OWNER_ID, "Finca Revertida", "Lurín, Lima", "Lima"));

            assertThat(result.isSuccess()).isTrue();
            status.setRollbackOnly();
        });

        assertThat(applicationEvents.stream(IntegrationEvent.class)).isEmpty();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT count(*) FROM farm WHERE name = ?", Integer.class, "Finca Revertida"))
                .isZero();
    }

    private long firstCropId() throws Exception {
        var body = mockMvc.perform(get("/api/v1/crops")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<?> ids = JsonPath.read(body, "$[*].id");
        return ((Number) ids.getFirst()).longValue();
    }

    private static long readId(MvcResult result) throws Exception {
        var body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private static String farmJson(String name) {
        return """
                {"ownerId":%d,"name":"%s","location":"Lurín, Lima","department":"Lima"}
                """.formatted(OWNER_ID, name);
    }

    private static String parcelJson(
            long farmId, String name, long cropId, String geometry) {
        return """
                {"farmId":%d,"name":"%s","geometry":%s,"cropId":%d,"phenologicalStage":"VEGETATIVE"}
                """.formatted(farmId, name, geometry, cropId);
    }

    private static String polygonJson(double minLongitude, double minLatitude, double size) {
        return """
                {"type":"Polygon","coordinates":[[[%s,%s],[%s,%s],[%s,%s],[%s,%s],[%s,%s]]]}"""
                .formatted(
                        minLongitude, minLatitude,
                        minLongitude + size, minLatitude,
                        minLongitude + size, minLatitude + size,
                        minLongitude, minLatitude + size,
                        minLongitude, minLatitude);
    }
}
