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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.GeneralMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.service.MetricService;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtClusterService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/clusters/{clusterId}/metrics")
@Transactional(propagation = Propagation.NEVER)
public class ClusterMetricController {

    /* Services */

    private final ClusterMetricService clusterMetricService;
    private final MetricService metricService;

    private final OVirtClusterService clusterService;

    /* Mappers */

    private final ClusterMetricMapper clusterMetricMapper;

    /* Util */

    private final ETagHelper eTagHelper;

    /* Create methods */

    @Operation(
            method = "POST", summary = "Create a value for certain metric for cluster",
            description = "This endpoint can be used by the administrator to create a new metric value for given cluster.",
            parameters = {
                    @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the value will be created for.", required = true),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Data transfer object containing essential information about created metric value, which in fact is identifier
                            of the metric, which the value is created for, and the actual value of the metric."""
            ),
            responses = {
                    @ApiResponse(responseCode = "204", description = "New metric value was created for given cluster successfully successfully in the database."),
                    @ApiResponse(responseCode = "404", description = "Cluster, which the metric value is to be created for, could not be found.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "409", description = "Metric has the value already defined for given cluster, and defining the next one is not possible.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createMetricValue(@PathVariable("clusterId") UUID clusterId,
                                                  @RequestBody @Validated CreateMetricValueDto createDto) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        clusterMetricService.createNewValueForMetric(cluster, createDto.metricId(), createDto.value());
        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @Operation(
            method = "GET", summary = "Get all values of the metrics, that were defined for the given cluster",
            description = "This endpoint can be used by the administrator to fetch all the metric values defined for given cluster.",
            parameters = {
                    @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the metric values will be fetched for.", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Non-empty page with metric values were found for given cluster and sent to the client successfully."),
                    @ApiResponse(responseCode = "204", description = "Empty page of metric values were found for given cluster, and as a result, 204 NO CONTENT is returned.",
                            content = @Content(schema = @Schema())),
                    @ApiResponse(responseCode = "404", description = "Cluster, which the metric value is to be created for, could not be found.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MetricValueDto>> getAllMetricValues(
            @PageableDefault Pageable pageable, @PathVariable("clusterId") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Page<ClusterMetric> clusterMetricPage = clusterMetricService.findAllMetricValuesForCluster(cluster, pageable);

        PageDto<MetricValueDto> listOfDTOs = new PageDto<>(
                clusterMetricPage.getContent().stream().map(clusterMetricMapper::clusterMetricToDto).toList(),
                new PageInfoDto(clusterMetricPage.getNumber(), clusterMetricPage.getNumberOfElements(),
                        clusterMetricPage.getTotalPages(), clusterMetricPage.getTotalElements())
        );

        if (listOfDTOs.items().isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @Operation(
            method = "GET", summary = "Get value of certain metric, that was defined for the given cluster",
            description = "This endpoint can be used by the administrator to fetch value of the certain metric, defined for given cluster.",
            parameters = {
                    @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the metric value will be fetched for.", required = true),
                    @Parameter(name = "metricId", in = ParameterIn.PATH, description = "Identifier of the metric, which is to be fetched.", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Value of given metric, defined for given cluster was found successfully."),
                    @ApiResponse(responseCode = "404", description = "Either cluster, metric or value of the metric defined for given cluster could not be found in the database!",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{metricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GeneralMetricValueDto> getClusterMetricDetails(@PathVariable("clusterId") UUID clusterId,
                                                                         @PathVariable("metricId") UUID metricId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Metric metric = metricService.findById(metricId);
        ClusterMetric foundMetricValue = clusterMetricService.findClusterMetricByClusterAndMetric(cluster, metric)
                .orElseThrow(() -> new ClusterMetricNotFoundException("Value of the metric %s for cluster %s could not be found!".formatted(metricId, clusterId)));

        return ResponseEntity.ok().eTag(eTagHelper.generateEtag(foundMetricValue))
                .body(clusterMetricMapper.clusterMetricToGeneralDto(foundMetricValue));
    }

    /* Update methods */

    @Operation(
            method = "PATCH", summary = "Update certain metric value for given cluster",
            description = "This endpoint can be used by the administrator to update existing metric value defined for given cluster.",
            parameters = {
                    @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the metric value will be updated for.", required = true),
                    @Parameter(name = "metricId", in = ParameterIn.PATH, description = "Identifier of the metric, which the value will be updated for given cluster.", required = true),
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = """
                            Data transfer object containing essential information about updated metric value, which in fact is only the new
                            value of the given metric."""
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Given metric value was found for given cluster and it was updated successfully."),
                    @ApiResponse(responseCode = "404", description = """
                            Cluster, which the metric value is to be created for, could not be found. Alternatively either metric,
                            with the identifier provided in the request body or actual value of the metric could not be found.""",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @PatchMapping(path = "/{metricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetricValueDto> updateMetricValue(
            @PathVariable("clusterId") UUID clusterId, @PathVariable("metricId") UUID metricId,
            @RequestBody @Validated ValueDto valueDto, @RequestHeader(HttpHeaders.IF_MATCH) String ifMatch) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        Metric metric = metricService.findById(metricId);

        ClusterMetric newMetricValue = clusterMetricMapper.valueDtoToClusterMetric(valueDto, UUID.fromString(cluster.id()), metric);
        ClusterMetric updatedMetric = clusterMetricService.updateMetricValue(valueDto.metricValueId(), newMetricValue, ifMatch);
        MetricValueDto dto = clusterMetricMapper.clusterMetricToDto(updatedMetric);

        return ResponseEntity.ok(dto);
    }

    /* Delete methods */

    @Operation(
            method = "DELETE", summary = "Remove value defined for given metric for cluster",
            description = """
                    This endpoint can be used by the administrator to remove certain metric value, that was defined for given
                    cluster, which exists in the oVirt system""",
            parameters = {
                    @Parameter(name = "clusterId", in = ParameterIn.PATH, description = "Identifier of the cluster, which the value to be removed is defined for.", required = true),
                    @Parameter(name = "metricId", in = ParameterIn.PATH, description = "Identifier of the metric, which is value is to be removed for given cluster.", required = true),
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Metric value, that was defined for given cluster was removed successfully."),
                    @ApiResponse(responseCode = "404", description = "Either cluster identifier with given identifier, or value of the given metric for given cluster could not be found.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Some other, unknown error occurred while processing the request.",
                            content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
            }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @DeleteMapping(path = "/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("clusterId") UUID clusterId,
                                             @PathVariable("metricId") UUID metricId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        clusterMetricService.deleteMetricValue(cluster, metricId);
        return ResponseEntity.noContent().build();
    }
}
