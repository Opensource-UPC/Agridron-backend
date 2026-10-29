package pe.com.agridron.field.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.com.agridron.field.application.commandservices.FarmCommandService;
import pe.com.agridron.field.application.queryservices.FarmQueryService;
import pe.com.agridron.field.domain.model.aggregates.Farm;
import pe.com.agridron.field.domain.model.commands.AssignFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.CreateFarmCommand;
import pe.com.agridron.field.domain.model.commands.RemoveFarmSupervisorCommand;
import pe.com.agridron.field.domain.model.commands.UpdateFarmCommand;
import pe.com.agridron.field.domain.model.queries.GetFarmByIdQuery;
import pe.com.agridron.field.domain.model.queries.GetFarmsByOwnerQuery;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FarmsController.class)
class FarmsControllerWebMvcTest {

    private static final String FARM_JSON = """
            {"ownerId":10,"name":"Finca El Sol","location":"Lurín, Lima","department":"Lima"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FarmCommandService farmCommandService;

    @MockitoBean
    private FarmQueryService farmQueryService;

    @Test
    void createsFarm() throws Exception {
        when(farmCommandService.handle(any(CreateFarmCommand.class)))
                .thenReturn(Result.success(farm(1L, Set.of())));

        mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FARM_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerId").value(10))
                .andExpect(jsonPath("$.name").value("Finca El Sol"))
                .andExpect(jsonPath("$.location").value("Lurín, Lima"))
                .andExpect(jsonPath("$.department").value("Lima"))
                .andExpect(jsonPath("$.supervisorIds", hasSize(0)));

        verify(farmCommandService).handle(
                new CreateFarmCommand(10L, "Finca El Sol", "Lurín, Lima", "Lima"));
    }

    @Test
    void rejectsFarmWithBlankName() throws Exception {
        mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"ownerId":10,"name":"  ","location":"Lima","department":"Lima"}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(farmCommandService, never()).handle(any(CreateFarmCommand.class));
    }

    @Test
    void rejectsFarmWithoutOwner() throws Exception {
        mockMvc.perform(post("/api/v1/farms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Finca","location":"Lima","department":"Lima"}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(farmCommandService, never()).handle(any(CreateFarmCommand.class));
    }

    @Test
    void listsFarmsByOwner() throws Exception {
        when(farmQueryService.handle(new GetFarmsByOwnerQuery(10L)))
                .thenReturn(List.of(farm(1L, Set.of()), farm(2L, Set.of(20L))));

        mockMvc.perform(get("/api/v1/farms")
                        .param("ownerId", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].supervisorIds[0]").value(20));

        verify(farmQueryService).handle(new GetFarmsByOwnerQuery(10L));
    }

    @Test
    void rejectsListWithoutOwnerId() throws Exception {
        mockMvc.perform(get("/api/v1/farms").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(farmQueryService, never()).handle(any(GetFarmsByOwnerQuery.class));
    }

    @Test
    void returnsFarmById() throws Exception {
        when(farmQueryService.handle(new GetFarmByIdQuery(1L)))
                .thenReturn(Result.success(farm(1L, Set.of(20L, 21L))));

        mockMvc.perform(get("/api/v1/farms/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.supervisorIds", hasSize(2)));
    }

    @Test
    void returnsNotFoundWhenFarmDoesNotExist() throws Exception {
        when(farmQueryService.handle(new GetFarmByIdQuery(99L)))
                .thenReturn(Result.failure(ApplicationError.notFound("Farm", "99")));

        mockMvc.perform(get("/api/v1/farms/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("FARM_NOT_FOUND"));
    }

    @Test
    void updatesFarm() throws Exception {
        when(farmCommandService.handle(any(UpdateFarmCommand.class)))
                .thenReturn(Result.success(farm(1L, Set.of())));

        mockMvc.perform(put("/api/v1/farms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Finca Nueva","location":"Ica","department":"Ica"}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(farmCommandService).handle(
                new UpdateFarmCommand(1L, "Finca Nueva", "Ica", "Ica"));
    }

    @Test
    void returnsConflictWhenUpdatingToDuplicatedName() throws Exception {
        when(farmCommandService.handle(any(UpdateFarmCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("Farm", "duplicated name")));

        mockMvc.perform(put("/api/v1/farms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Finca Nueva","location":"Ica","department":"Ica"}
                                """)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("FARM_CONFLICT"));
    }

    @Test
    void assignsSupervisor() throws Exception {
        when(farmCommandService.handle(any(AssignFarmSupervisorCommand.class)))
                .thenReturn(Result.success(farm(1L, Set.of(20L, 21L))));

        mockMvc.perform(post("/api/v1/farms/1/supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"supervisorId\":20}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisorIds", hasSize(2)));

        verify(farmCommandService).handle(new AssignFarmSupervisorCommand(1L, 20L));
    }

    @Test
    void rejectsAssignmentWithoutSupervisor() throws Exception {
        mockMvc.perform(post("/api/v1/farms/1/supervisors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(farmCommandService, never()).handle(any(AssignFarmSupervisorCommand.class));
    }

    @Test
    void removesSupervisor() throws Exception {
        when(farmCommandService.handle(any(RemoveFarmSupervisorCommand.class)))
                .thenReturn(Result.success(farm(1L, Set.of())));

        mockMvc.perform(delete("/api/v1/farms/1/supervisors/20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.supervisorIds", hasSize(0)));

        verify(farmCommandService).handle(new RemoveFarmSupervisorCommand(1L, 20L));
    }

    private static Farm farm(Long id, Set<Long> supervisorIds) {
        return Farm.reconstitute(id, 10L, "Finca El Sol", "Lurín, Lima", "Lima", supervisorIds);
    }
}
