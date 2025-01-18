package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.MetricMapper;
import pl.lodz.p.it.eduvirt.service.MetricService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/metrics")
@Transactional(propagation = Propagation.NEVER)
public class MetricController {

    /* Services */

    private final MetricService metricService;

    /* Mappers */

    private final MetricMapper metricMapper;

    /* Create methods  */

    @Operation(
        method = "POST", summary = "Create a new metric",
        description = "This endpoint can be used by the administrator to create a new metric.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
            Data transfer object containing essential information about created metric, which in fact is only the name
            of the created metric, and its category (which is used by the frontend application for selecting correct set
            of units)."""
        ),
        responses = {
            @ApiResponse(responseCode = "204", description = "New metric, with given name and category, was created successfully in the database."),
            @ApiResponse(responseCode = "409", description = "Metric with given name already exists, and that name cannot be re-used to identifier other metric.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewMetric(@RequestBody @Validated CreateMetricDto createDto) {
        metricService.createNewMetric(createDto.name(), createDto.category());
        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @Operation(
        method = "GET", summary = "Get all metric",
        description = """
            This endpoint can be used by the administrator to fetch a list of metrics, which could be
            used to defined a value for course / cluster (including dividing them into pages).""",
        responses = {
            @ApiResponse(responseCode = "200", description = "Given page with reservation was found in the database and sent to the client successfully."),
            @ApiResponse(responseCode = "204", description = "No metrics were found (on the given page of the of given size).",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MetricDto>> getAllMetrics(@PageableDefault Pageable pageable) {
        Page<Metric> metricPage = metricService.findAllMetrics(pageable);
        PageDto<MetricDto> listOfDTOs = new PageDto<>(
                metricPage.getContent().stream().map(metricMapper::metricToDto).toList(),
                new PageInfoDto(metricPage.getNumber(), metricPage.getNumberOfElements(),
                        metricPage.getTotalPages(), metricPage.getTotalElements())
        );

        if (metricPage.getContent().isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    /* Delete methods */

    @Operation(
        method = "POST", summary = "Remove metric",
        description = """
            This endpoint can be used by the administrator to remove certain metric, and all the values associated
            with that metric for every cluster in the oVirt system""",
        parameters = {
            @Parameter(name = "metricId", in = ParameterIn.PATH, description = "Identifier of the metric, which is to be removed.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "New metric, with given name and category, was created successfully in the database."),
            @ApiResponse(responseCode = "409", description = "Metric with given name already exists, and that name cannot be re-used to identifier other metric.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @DeleteMapping(path = "/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable UUID metricId) {
        metricService.deleteMetric(metricId);
        return ResponseEntity.noContent().build();
    }
}
