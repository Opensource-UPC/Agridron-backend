package pe.com.agridron.analytics.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.analytics.application.commandservices.MissionReportCommandService;
import pe.com.agridron.analytics.application.queryservices.MissionReportQueryService;
import pe.com.agridron.analytics.domain.model.commands.DeleteMissionReportCommand;
import pe.com.agridron.analytics.domain.model.queries.GetMissionReportByIdQuery;
import pe.com.agridron.analytics.domain.model.valueobjects.ReportType;
import pe.com.agridron.analytics.interfaces.rest.resources.AddOperationalMetricResource;
import pe.com.agridron.analytics.interfaces.rest.resources.CreateMissionReportResource;
import pe.com.agridron.analytics.interfaces.rest.resources.MissionReportResource;
import pe.com.agridron.analytics.interfaces.rest.transform.AddOperationalMetricCommandFromResourceAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.GenerateMissionReportCommandFromResourceAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.MissionReportResourceFromEntityAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.MissionReportSearchCriteriaFromRequestAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/mission-reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mission reports", description = "Mission reports and their operational metrics")
public class MissionReportsController {

    private final MissionReportCommandService missionReportCommandService;
    private final MissionReportQueryService missionReportQueryService;

    public MissionReportsController(
            MissionReportCommandService missionReportCommandService,
            MissionReportQueryService missionReportQueryService) {
        this.missionReportCommandService = missionReportCommandService;
        this.missionReportQueryService = missionReportQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Generate a mission report")
    public ResponseEntity<?> generateMissionReport(@Valid @RequestBody CreateMissionReportResource resource) {
        var command = GenerateMissionReportCommandFromResourceAssembler.toCommandFromResource(
                resource, Instant.now());
        var result = missionReportCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MissionReportResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Search mission reports",
            description = "All filters are optional. 'from' and 'to' are inclusive dates (UTC) applied to the generation date.")
    public ResponseEntity<List<MissionReportResource>> getMissionReports(
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) List<Long> parcelIds,
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var criteria = MissionReportSearchCriteriaFromRequestAssembler.toCriteria(
                missionId, parcelIds, type, from, to);
        var reports = missionReportQueryService.handle(criteria).stream()
                .map(MissionReportResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get a mission report by id")
    public ResponseEntity<?> getMissionReportById(@PathVariable Long reportId) {
        var result = missionReportQueryService.handle(new GetMissionReportByIdQuery(reportId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MissionReportResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }

    @PostMapping(value = "/{reportId}/metrics", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add an operational metric to a report")
    public ResponseEntity<?> addOperationalMetric(
            @PathVariable Long reportId, @Valid @RequestBody AddOperationalMetricResource resource) {
        var command = AddOperationalMetricCommandFromResourceAssembler.toCommandFromResource(reportId, resource);
        var result = missionReportCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MissionReportResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete a mission report")
    public ResponseEntity<?> deleteMissionReport(@PathVariable Long reportId) {
        var result = missionReportCommandService.handle(new DeleteMissionReportCommand(reportId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, deletedId -> null, HttpStatus.NO_CONTENT);
    }
}
