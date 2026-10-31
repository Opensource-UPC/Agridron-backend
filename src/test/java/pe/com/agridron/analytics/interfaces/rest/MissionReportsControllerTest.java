package pe.com.agridron.analytics.interfaces.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import pe.com.agridron.analytics.application.commandservices.MissionReportCommandService;
import pe.com.agridron.analytics.application.queryservices.MissionReportQueryService;
import pe.com.agridron.analytics.domain.model.aggregates.MissionReport;
import pe.com.agridron.analytics.domain.model.commands.DeleteMissionReportCommand;
import pe.com.agridron.analytics.domain.model.commands.GenerateMissionReportCommand;
import pe.com.agridron.analytics.domain.model.queries.GetMissionReportByIdQuery;
import pe.com.agridron.analytics.domain.model.queries.MissionReportSearchCriteria;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.shared.application.result.ApplicationError;
import pe.com.agridron.shared.application.result.Result;
import pe.com.agridron.shared.interfaces.rest.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MissionReportsControllerTest {

    @Mock
    private MissionReportCommandService commandService;

    @Mock
    private MissionReportQueryService queryService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        var validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        validator.afterPropertiesSet();
        var localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH);
        mockMvc = MockMvcBuilders.standaloneSetup(new MissionReportsController(commandService, queryService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setLocaleResolver(localeResolver)
                .build();
    }

    private static MissionReport report() {
        var report = MissionReport.reconstitute(
                5L, 1L, 2L, ReportType.MISSION_SUMMARY, new BigDecimal("12.50"), new BigDecimal("38.00"),
                "ok", Instant.parse("2026-09-20T15:30:00Z"), null);
        return report;
    }

    @Test
    void generatesAReportAndReturnsCreated() throws Exception {
        when(commandService.handle(any(GenerateMissionReportCommand.class))).thenReturn(Result.success(report()));

        mockMvc.perform(post("/api/v1/mission-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"missionId":1,"parcelId":2,"type":"MISSION_SUMMARY",
                                 "treatedArea":12.5,"appliedVolume":38,"observations":"ok"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.missionId").value(1))
                .andExpect(jsonPath("$.type").value("MISSION_SUMMARY"));
    }

    @Test
    void rejectsInvalidBodyWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/mission-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"missionId":1,"parcelId":2,"type":"MISSION_SUMMARY",
                                 "treatedArea":-1,"appliedVolume":38}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mapsConflictToHttp409() throws Exception {
        when(commandService.handle(any(GenerateMissionReportCommand.class)))
                .thenReturn(Result.failure(ApplicationError.conflict("Report", "already exists")));

        mockMvc.perform(post("/api/v1/mission-reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"missionId":1,"parcelId":2,"type":"MISSION_SUMMARY",
                                 "treatedArea":1,"appliedVolume":1}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REPORT_CONFLICT"));
    }

    @Test
    void getsAReportById() throws Exception {
        when(queryService.handle(new GetMissionReportByIdQuery(5L))).thenReturn(Result.success(report()));

        mockMvc.perform(get("/api/v1/mission-reports/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.metrics").isArray());
    }

    @Test
    void returnsNotFoundForUnknownReport() throws Exception {
        when(queryService.handle(new GetMissionReportByIdQuery(99L)))
                .thenReturn(Result.failure(ApplicationError.notFound("Report", "99")));

        mockMvc.perform(get("/api/v1/mission-reports/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_NOT_FOUND"));
    }

    @Test
    void searchesReportsWithFiltersAndConvertsDatesToUtcRange() throws Exception {
        when(queryService.handle(any(MissionReportSearchCriteria.class))).thenReturn(List.of(report()));

        mockMvc.perform(get("/api/v1/mission-reports")
                        .param("parcelIds", "2,3")
                        .param("type", "MISSION_SUMMARY")
                        .param("from", "2026-09-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        var captor = ArgumentCaptor.forClass(MissionReportSearchCriteria.class);
        verify(queryService).handle(captor.capture());
        var criteria = captor.getValue();
        assertThat(criteria.parcelIds()).containsExactly(2L, 3L);
        assertThat(criteria.type()).isEqualTo(ReportType.MISSION_SUMMARY);
        assertThat(criteria.from()).isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(criteria.to()).isEqualTo(Instant.parse("2026-10-01T00:00:00Z"));
    }

    @Test
    void deletesAReport() throws Exception {
        when(commandService.handle(new DeleteMissionReportCommand(5L))).thenReturn(Result.success(5L));

        mockMvc.perform(delete("/api/v1/mission-reports/5"))
                .andExpect(status().isNoContent());
    }
}
