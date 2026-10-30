package pe.com.agridron.field;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pe.com.agridron.field.application.commandservices.FarmCommandService;
import pe.com.agridron.field.application.commandservices.ParcelCommandService;
import pe.com.agridron.field.domain.TestGeometries;
import pe.com.agridron.field.domain.model.aggregates.Parcel;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.domain.model.commands.CreateParcelCommand;
import pe.com.agridron.field.domain.model.valueobjects.FieldGeometry;
import pe.com.agridron.field.domain.ports.ParcelUsagePort;
import pe.com.agridron.support.AbstractPostgisIntegrationTest;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the FM-006 safeguard: a parcel with active missions cannot be deactivated.
 *
 * <p>The real mission state belongs to Flight Operations, so the port is replaced with a
 * mock to simulate both scenarios. The HTTP response and the persisted status are checked
 * because the rule must be enforced at the application boundary.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ParcelUsageE2ETest extends AbstractPostgisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FarmCommandService farmCommandService;

    @Autowired
    private ParcelCommandService parcelCommandService;

    @MockitoBean
    private ParcelUsagePort parcelUsagePort;

    @Test
    void rejectsDeactivationWhenParcelHasActiveMissions() throws Exception {
        var parcel = createParcel();
        when(parcelUsagePort.hasActiveMissions(parcel.getId())).thenReturn(true);

        mockMvc.perform(patch("/api/v1/parcels/{parcelId}/status", parcel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("PARCEL_INVALID_STATE"));

        mockMvc.perform(get("/api/v1/parcels/{parcelId}", parcel.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void allowsDeactivationWhenParcelHasNoActiveMissions() throws Exception {
        var parcel = createParcel();
        when(parcelUsagePort.hasActiveMissions(parcel.getId())).thenReturn(false);

        mockMvc.perform(patch("/api/v1/parcels/{parcelId}/status", parcel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INACTIVE\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    private Parcel createParcel() {
        var farm = farmCommandService.handle(new CreateFarmCommand(
                        9001L, "Finca de prueba", "Lurín, Lima", "Lima"))
                .toOptional()
                .orElseThrow();

        return parcelCommandService.handle(new CreateParcelCommand(
                        farm.getId(), "Lote de prueba", geometry(0.01), null, null))
                .toOptional()
                .orElseThrow();
    }

    private static FieldGeometry geometry(double size) {
        return FieldGeometry.from(TestGeometries.wgs84Square(-77.042, -12.046, size));
    }
}
