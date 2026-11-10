package pe.com.agridron.analytics.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.analytics.application.commandservices.MissionHistoryCommandService;
import pe.com.agridron.analytics.application.queryservices.MissionHistoryQueryService;
import pe.com.agridron.analytics.domain.model.queries.GetMissionHistoryByIdQuery;
import pe.com.agridron.analytics.domain.model.valueobjects.MissionFinalStatus;
import pe.com.agridron.analytics.interfaces.rest.resources.MissionHistoryResource;
import pe.com.agridron.analytics.interfaces.rest.resources.RegisterMissionHistoryResource;
import pe.com.agridron.analytics.interfaces.rest.transform.MissionHistoryResourceFromEntityAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.MissionHistorySearchCriteriaFromRequestAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.RegisterMissionHistoryCommandFromResourceAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/mission-histories", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mission histories", description = "Archived records of finished missions")
public class MissionHistoriesController {

    private final MissionHistoryCommandService missionHistoryCommandService;
    private final MissionHistoryQueryService missionHistoryQueryService;

    public MissionHistoriesController(
            MissionHistoryCommandService missionHistoryCommandService,
            MissionHistoryQueryService missionHistoryQueryService) {
        this.missionHistoryCommandService = missionHistoryCommandService;
        this.missionHistoryQueryService = missionHistoryQueryService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a finished mission in the history")
    public ResponseEntity<?> registerMissionHistory(@Valid @RequestBody RegisterMissionHistoryResource resource) {
        var command = RegisterMissionHistoryCommandFromResourceAssembler.toCommandFromResource(resource);
        var result = missionHistoryCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MissionHistoryResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Search the mission history",
            description = "All filters are optional. 'from' and 'to' are inclusive dates (UTC) applied to the completion date.")
    public ResponseEntity<List<MissionHistoryResource>> getMissionHistories(
            @RequestParam(required = false) Long missionId,
            @RequestParam(required = false) List<Long> parcelIds,
            @RequestParam(required = false) MissionFinalStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var criteria = MissionHistorySearchCriteriaFromRequestAssembler.toCriteria(
                missionId, parcelIds, status, from, to);
        var histories = missionHistoryQueryService.handle(criteria).stream()
                .map(MissionHistoryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(histories);
    }

    @GetMapping("/{historyId}")
    @Operation(summary = "Get a mission history record by id")
    public ResponseEntity<?> getMissionHistoryById(@PathVariable Long historyId) {
        var result = missionHistoryQueryService.handle(new GetMissionHistoryByIdQuery(historyId));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, MissionHistoryResourceFromEntityAssembler::toResourceFromEntity, HttpStatus.OK);
    }
}
