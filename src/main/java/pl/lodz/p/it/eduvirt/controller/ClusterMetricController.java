package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.service.impl.OVirtClusterServiceImpl;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/clusters/{clusterId}/metrics")
@Transactional(propagation = Propagation.NEVER)
public class ClusterMetricController {

    /* Services */

    private final ClusterMetricService clusterMetricService;

    private final OVirtClusterServiceImpl oVirtClusterServiceImpl;

    /* Mappers */

    private final ClusterMetricMapper clusterMetricMapper;

    /* Create methods */

    @PreAuthorize("hasAuthority('administrator')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createMetricValue(@PathVariable("clusterId") UUID clusterId,
                                                  @RequestBody @Validated CreateMetricValueDto createDto) {
        Cluster cluster = oVirtClusterServiceImpl.findClusterById(clusterId);
        clusterMetricService.createNewValueForMetric(cluster, createDto.metricId(), createDto.value());
        return ResponseEntity.noContent().build();
    }

    /* Read methods */

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MetricValueDto>> getAllMetricValues(
            @RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
            @PathVariable("clusterId") UUID clusterId) {
        try {
            Cluster cluster = oVirtClusterServiceImpl.findClusterById(clusterId);
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<ClusterMetric> clusterMetricPage = clusterMetricService.findAllMetricValuesForCluster(cluster, pageable);

            PageDto<MetricValueDto> listOfDTOs = new PageDto<>(
                    clusterMetricPage.getContent().stream().map(clusterMetricMapper::clusterMetricToDto).toList(),
                    new PageInfoDto(clusterMetricPage.getNumber(), clusterMetricPage.getNumberOfElements(),
                            clusterMetricPage.getTotalPages(), clusterMetricPage.getTotalElements())
            );

            if (listOfDTOs.items().isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDTOs);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.noContent().build();
        }
    }

    /* Update methods */

    @PreAuthorize("hasAuthority('administrator')")
    @PatchMapping(path = "/{metricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetricValueDto> updateMetricValue(@PathVariable("clusterId") UUID clusterId,
                                                            @PathVariable("metricId") UUID metricId,
                                                            @RequestBody @Validated ValueDto valueDto) {
        Cluster cluster = oVirtClusterServiceImpl.findClusterById(clusterId);
        ClusterMetric updatedMetric = clusterMetricService.updateMetricValue(cluster, metricId, valueDto.value());
        MetricValueDto dto = clusterMetricMapper.clusterMetricToDto(updatedMetric);
        return ResponseEntity.ok(dto);
    }

    /* Delete methods */

    @PreAuthorize("hasAuthority('administrator')")
    @DeleteMapping(path = "/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("clusterId") UUID clusterId,
                                             @PathVariable("metricId") UUID metricId) {
        Cluster cluster = oVirtClusterServiceImpl.findClusterById(clusterId);
        clusterMetricService.deleteMetricValue(cluster, metricId);
        return ResponseEntity.noContent().build();
    }
}
