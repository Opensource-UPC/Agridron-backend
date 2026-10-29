package pe.com.agridron.field;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.support.AbstractPostgisIntegrationTest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of the Field Management REST API against a real PostGIS instance.
 *
 * <p>Every test runs inside a transaction that is rolled back, so the seeded crop catalog
 * stays untouched and tests do not depend on execution order.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FieldManagementE2ETest extends AbstractPostgisIntegrationTest {

    private static final long OWNER_ID = 1001L;
    private static final long SUPERVISOR_ID = 2001L;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runsTheCompleteFieldManagementFlow() throws Exception {
        long cropId = firstCropId();

        var farmId = readId(mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmJson("Finca E2E"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value((int) OWNER_ID))
                .andExpect(jsonPath("$.name").value("Finca E2E"))
                .andReturn());

        mockMvc.perform(get("/api/v1/farms")
                        .param("ownerId", String.valueOf(OWNER_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int) farmId));

        mockMvc.perform(get("/api/v1/farms/{farmId}", farmId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Finca E2E"));

        mockMvc.perform(put("/api/v1/farms/{farmId}", farmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmJson("Finca E2E Renombrada"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Finca E2E Renombrada"));

        mockMvc.perform(post("/api/v1/farms/{farmId}/supervisors", farmId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisorId\":%d}".formatted(SUPERVISOR_ID))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisorIds", hasSize(1)))
                .andExpect(jsonPath("$.supervisorIds[0]").value((int) SUPERVISOR_ID));

        mockMvc.perform(delete("/api/v1/farms/{farmId}/supervisors/{supervisorId}",
                        farmId, SUPERVISOR_ID)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisorIds", hasSize(0)));

        var parcelId = readId(mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parcelJson(
                                farmId, "Lote E2E", cropId, polygonJson(-77.050, -12.050, 0.01)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmId").value((int) farmId))
                .andExpect(jsonPath("$.cropId").value((int) cropId))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.geometry.type").value("Polygon"))
                .andExpect(jsonPath("$.areaHectares").isNumber())
                .andReturn());

        mockMvc.perform(get("/api/v1/parcels")
                        .param("farmId", String.valueOf(farmId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int) parcelId));

        mockMvc.perform(get("/api/v1/parcels/{parcelId}", parcelId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lote E2E"));

        mockMvc.perform(put("/api/v1/parcels/{parcelId}", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Lote E2E Actualizado","cropId":%d,"phenologicalStage":"FLOWERING"}
                                """.formatted(cropId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Lote E2E Actualizado"))
                .andExpect(jsonPath("$.phenologicalStage").value("FLOWERING"));

        mockMvc.perform(put("/api/v1/parcels/{parcelId}/geometry", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(polygonJson(-77.060, -12.060, 0.03))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geometry.coordinates[0]", hasSize(5)));

        mockMvc.perform(patch("/api/v1/parcels/{parcelId}/status", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        var areaId = readId(mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":%d,"geometry":%s}
                                """.formatted(parcelId, polygonJson(-77.045, -12.045, 0.002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parcelId").value((int) parcelId))
                .andExpect(jsonPath("$.areaHectares").isNumber())
                .andReturn());

        mockMvc.perform(get("/api/v1/fumigation-areas")
                        .param("parcelId", String.valueOf(parcelId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value((int) areaId));

        mockMvc.perform(get("/api/v1/fumigation-areas/{areaId}", areaId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) areaId));

        mockMvc.perform(put("/api/v1/fumigation-areas/{areaId}", areaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(polygonJson(-77.044, -12.044, 0.001))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value((int) areaId));
    }

    @Test
    void returnsProblemDetailsForInvalidRequests() throws Exception {
        long cropId = firstCropId();
        long farmId = createFarm("Finca Errores");
        long parcelId = createParcel(farmId, cropId, "Lote Errores", polygonJson(-77.050, -12.050, 0.01));

        mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parcelJson(
                                farmId, "Lote Inválido", cropId,
                                openRingJson(-77.050, -12.050, 0.01)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_GEOMETRY"));

        mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmJson("Finca Errores"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FARM_CONFLICT"));

        mockMvc.perform(post("/api/v1/fumigation-areas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parcelId":%d,"geometry":%s}
                                """.formatted(parcelId, polygonJson(-77.030, -12.030, 0.002)))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));

        mockMvc.perform(get("/api/v1/farms/999999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FARM_NOT_FOUND"));

        mockMvc.perform(get("/api/v1/parcels/999999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PARCEL_NOT_FOUND"));
    }

    private long firstCropId() throws Exception {
        var body = mockMvc.perform(get("/api/v1/crops").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        List<?> ids = JsonPath.read(body, "$[*].id");
        return ((Number) ids.getFirst()).longValue();
    }

    private long createFarm(String name) throws Exception {
        return readId(mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(farmJson(name))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn());
    }

    private long createParcel(long farmId, long cropId, String name, String geometry) throws Exception {
        return readId(mockMvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(parcelJson(farmId, name, cropId, geometry))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn());
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

    private static String openRingJson(double minLongitude, double minLatitude, double size) {
        return """
                {"type":"Polygon","coordinates":[[[%s,%s],[%s,%s],[%s,%s],[%s,%s],[%s,%s]]]}"""
                .formatted(
                        minLongitude, minLatitude,
                        minLongitude + size, minLatitude,
                        minLongitude + size, minLatitude + size,
                        minLongitude, minLatitude + size,
                        minLongitude, minLatitude + size / 2);
    }
}
