package pe.com.agridron.analytics.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.com.agridron.analytics.application.commandservices.PerformanceIndicatorCommandService;
import pe.com.agridron.analytics.application.queryservices.PerformanceIndicatorQueryService;
import pe.com.agridron.analytics.domain.model.aggregates.PerformanceIndicator;
import pe.com.agridron.analytics.domain.model.queries.GetPerformanceIndicatorsByPeriodQuery;
import pe.com.agridron.analytics.interfaces.rest.resources.CalculatePerformanceIndicatorsResource;
import pe.com.agridron.analytics.interfaces.rest.resources.PerformanceIndicatorResource;
import pe.com.agridron.analytics.interfaces.rest.transform.CalculatePerformanceIndicatorsCommandFromResourceAssembler;
import pe.com.agridron.analytics.interfaces.rest.transform.PerformanceIndicatorResourceFromEntityAssembler;
import pe.com.agridron.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/performance-indicators", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Performance indicators", description = "Monthly performance indicators of the operation")
public class PerformanceIndicatorsController {

    private final PerformanceIndicatorCommandService performanceIndicatorCommandService;
    private final PerformanceIndicatorQueryService performanceIndicatorQueryService;

    public PerformanceIndicatorsController(
            PerformanceIndicatorCommandService performanceIndicatorCommandService,
            PerformanceIndicatorQueryService performanceIndicatorQueryService) {
        this.performanceIndicatorCommandService = performanceIndicatorCommandService;
        this.performanceIndicatorQueryService = performanceIndicatorQueryService;
    }

    @GetMapping
    @Operation(summary = "List performance indicators",
            description = "Optionally filtered by period in YYYY-MM format.")
    public ResponseEntity<?> getPerformanceIndicators(@RequestParam(required = false) String period) {
        var result = performanceIndicatorQueryService.handle(new GetPerformanceIndicatorsByPeriodQuery(period));
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, PerformanceIndicatorsController::toResources, HttpStatus.OK);
    }

    @PostMapping(value = "/calculations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Calculate the performance indicators of a period",
            description = "Recalculates the indicators of the month from the archived missions and their reports.")
    public ResponseEntity<?> calculatePerformanceIndicators(
            @Valid @RequestBody CalculatePerformanceIndicatorsResource resource) {
        var command = CalculatePerformanceIndicatorsCommandFromResourceAssembler.toCommandFromResource(
                resource, Instant.now());
        var result = performanceIndicatorCommandService.handle(command);
        return ResponseEntityAssembler.toResponseEntityFromResult(
                result, PerformanceIndicatorsController::toResources, HttpStatus.CREATED);
    }

    private static List<PerformanceIndicatorResource> toResources(List<PerformanceIndicator> indicators) {
        return indicators.stream()
                .map(PerformanceIndicatorResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
