package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.CreateMaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.MaintenanceIntervalMapper;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/maintenance-intervals")
@Transactional(propagation = Propagation.NEVER)
public class MaintenanceIntervalController {

    /* Services */

    private final MaintenanceIntervalService maintenanceIntervalService;

    private final OVirtClusterService clusterService;

    /* Mappers */

    private final MaintenanceIntervalMapper maintenanceIntervalMapper;

    /* Create methods */

    @Operation(
        method = "POST", summary = "Create a new maintenance interval for cluster",
        description = "This endpoint can be used by the administrator to create a new maintenance interval for the given cluster.",
        parameters = {
            @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the new maintenance interval will be created for.", required = true),
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
            Data transfer object containing essential information about created maintenance interval, like the time
            window during which the maintenance interval take effect, its cause and more
            descriptive description (if provided at all)."""
        ),
        responses = {
            @ApiResponse(responseCode = "204", description = "New maintenance interval for the given cluster was created successfully."),
            @ApiResponse(responseCode = "400", description = """
                Maintenance interval could not be created, since the cluster which the maintenance interval
                is to be made for, could not be found, or time window data is invalid.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                Maintenance interval for the given cluster could not be created since there is a maintenance interval for given cluster
                or system already defined that overlaps given time window.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(path = "/cluster/{clusterId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewClusterMaintenanceInterval(
            @PathVariable("clusterId") UUID clusterId,
            @RequestBody @Validated CreateMaintenanceIntervalDto createDto) {
        Cluster foundCluster = clusterService.findClusterById(clusterId);
        maintenanceIntervalService.createClusterMaintenanceInterval(
                foundCluster,
                createDto.cause(),
                createDto.description(),
                createDto.beginAt(),
                createDto.endAt());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        method = "POST", summary = "Create a new system maintenance interval",
        description = "This endpoint can be used by the administrator to create a new maintenance interval for the entire oVirt system (for situation where no cluster will be available).",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
            Data transfer object containing essential information about created maintenance interval, like the time
            window during which the maintenance interval take effect, its cause and more
            descriptive description (if provided at all)."""
        ),
        responses = {
            @ApiResponse(responseCode = "204", description = "New maintenance interval for the system was created successfully."),
            @ApiResponse(responseCode = "400", description = """
                Maintenance interval could not be created, since time window data is invalid.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "409", description = """
                Maintenance interval for the given cluster could not be created since there is a maintenance interval for the system
                already defined that overlaps given time window.""",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(path = "/system", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewSystemMaintenanceInterval(
            @RequestBody @Validated CreateMaintenanceIntervalDto createDto) {
        maintenanceIntervalService.createSystemMaintenanceInterval(
                createDto.cause(),
                createDto.description(),
                createDto.beginAt(),
                createDto.endAt());
        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @Operation(
        method = "GET", summary = "Get maintenance intervals (either active or inactive) for cluster / system",
        description = "This endpoint can be used to fetch either active or inactive maintenance intervals, defined for given cluster (or system, if the cluster identifier is null) (that includes pagination and sorting as well).",
        parameters = {
            @Parameter(name = "clusterId", in = ParameterIn.QUERY, description = "Identifier of the cluster, which the maintenance intervals are to be fetched from the database. If that identifier is not defined, then maintenance intervals for system are fetched instead."),
            @Parameter(name = "active", in = ParameterIn.QUERY, description = """
                Status of the reservations that are to be fetched from the database, expressed as boolean value.
                If true then active intervals are fetched, in the other case inactive intervals are fetched.""", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Page with maintenance intervals for the given cluster / system was found and sent to the client successfully."),
            @ApiResponse(responseCode = "204", description = """
                No maintenance intervals were found for given cluster / system, and as a result empty list of maintenance intervals
                is sent to the client, or some invalid pagination parameters were passed as the values in the pageable object.""",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Cluster, which the maintenance intervals are to be fetched for, could not be found in the database.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MaintenanceIntervalDto>> getAllMaintenanceIntervals(
            @PageableDefault Pageable pageable,
            @RequestParam(name = "clusterId", required = false) UUID clusterId,
            @RequestParam(name = "active", required = false, defaultValue = "true") boolean active) {
        if (clusterId != null) clusterService.findClusterById(clusterId);

        Page<MaintenanceInterval> maintenanceIntervalPage = maintenanceIntervalService
                .findAllMaintenanceIntervals(clusterId, active, pageable);

        PageDto<MaintenanceIntervalDto> listOfDtos = new PageDto<>(
                maintenanceIntervalPage.getContent().stream().map(maintenanceIntervalMapper::maintenanceIntervalToDto).toList(),
                new PageInfoDto(maintenanceIntervalPage.getNumber(), maintenanceIntervalPage.getNumberOfElements(),
                        maintenanceIntervalPage.getTotalPages(), maintenanceIntervalPage.getTotalElements())
        );

        if (maintenanceIntervalPage.getContent().isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @Operation(
        method = "GET", summary = "Get maintenance intervals for the cluster / system in given time window.",
        description = "This endpoint can be used to fetch maintenance intervals, defined for certain cluster / system in the specified time window.",
        parameters = {
            @Parameter(name = "clusterId", in = ParameterIn.QUERY, description = "Identifier of the cluster, which the maintenance intervals are to be fetched from the database. If not provided, then maintenance intervals for system are fetched."),
            @Parameter(name = "active", in = ParameterIn.QUERY, description = """
                Status of the reservations that are to be fetched from the database, expressed as boolean value.
                If true then active intervals are fetched, in the other case inactive intervals are fetched.""", required = true),
            @Parameter(name = "start", description = "Timestamp of the start of the time window, which the searched maintenance intervals overlaps with."),
            @Parameter(name = "end", description = "Timestamp of the end of the time window, which the searched maintenance intervals overlaps with.")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Some maintenance intervals, overlapping given time window, were found for given cluster / system and sent to the client successfully."),
            @ApiResponse(responseCode = "204", description = "No maintenance, overlapping given time window, intervals were found for the given cluster / system, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Cluster, which the maintenance intervals are to be fetched for, could not be found in the database.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/time-period")
    public ResponseEntity<List<MaintenanceIntervalDto>> getMaintenanceIntervalsWithinTimePeriod(
            @RequestParam(value = "clusterId", required = false) UUID clusterId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        if (clusterId != null) clusterService.findClusterById(clusterId);

        List<MaintenanceInterval> foundIntervals = maintenanceIntervalService
                .findAllMaintenanceIntervalsInTimePeriod(clusterId, start, end);

        List<MaintenanceIntervalDto> listOfDtos = foundIntervals.stream()
                .map(maintenanceIntervalMapper::maintenanceIntervalToDto).toList();

        if (foundIntervals.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDtos);
    }

    @Operation(
        method = "GET", summary = "Get detailed information about certain maintenance interval",
        description = "This endpoint can be used to fetch detailed information about certain maintenance interval, identified with the given identifier.",
        parameters = {
            @Parameter(name = "intervalId", in = ParameterIn.PATH, description = "Identifier of the maintenance interval, which detailed information is to be found in the database.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Maintenance interval, identified with given identifier was found and sent to the client successfully."),
            @ApiResponse(responseCode = "404", description = "Maintenance interval, identified with given identifier could not be found in the database, or currently authenticated user did not have privileges to access it.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MaintenanceIntervalDetailsDto> getMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        try {
            MaintenanceInterval foundInterval = maintenanceIntervalService.findMaintenanceInterval(intervalId)
                    .orElseThrow(() -> new MaintenanceIntervalNotFound(intervalId));

            MaintenanceIntervalDetailsDto outputDto = maintenanceIntervalMapper
                    .maintenanceIntervalToDetailsDto(foundInterval);

            return ResponseEntity.ok(outputDto);
        } catch (MaintenanceIntervalNotFound exception) {
            return ResponseEntity.notFound().build();
        }
    }

    /* Delete methods */

    @Operation(
        method = "POST", summary = "Finish / remove certain maintenance interval.",
        description = """
                This endpoint can be used to finish / remove certain maintenance interval (depending on the time of the invocation of this method).
                If this method is invoked after the start of the maintenance interval, it marks the reservation as finished and changes its end time.
                In the latter case, it removes the maintenance interval from the database altogether.""",
        parameters = {
            @Parameter(name = "intervalId", in = ParameterIn.PATH, description = "Identifier of the maintenance interval, which is to be finished or removed (depending on the current time).", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "204", description = """
                Maintenance interval with given identifier was found, and finished / removed from the database successfully. In the first case,
                reservation stays in the database."""),
            @ApiResponse(responseCode = "404", description = "Maintenance interval identified with the given identifier could not be found.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(path = "/{intervalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> finishMaintenanceInterval(@PathVariable("intervalId") UUID intervalId) {
        maintenanceIntervalService.finishMaintenanceInterval(intervalId);
        return ResponseEntity.noContent().build();
    }
}
