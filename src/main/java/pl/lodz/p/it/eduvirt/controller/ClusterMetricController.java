package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.ValueDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.mappers.ClusterMetricMapper;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/clusters/{clusterId}/metrics")
public class ClusterMetricController {

    private final ClusterMetricService clusterMetricService;

    private final ClusterMetricMapper clusterMetricMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createMetricValue(@PathVariable("clusterId") UUID clusterId,
                                               @RequestBody CreateMetricValueDto createDto) {
        clusterMetricService.createNewValueForMetric(clusterId, createDto.metricId(), createDto.value());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MetricValueDto>> getAllMetricValues(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                                      @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                                      @PathVariable("clusterId") UUID clusterId) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            Page<ClusterMetric> clusterMetricPage = clusterMetricService.findAllMetricValuesForCluster(clusterId, pageable);

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

    @PatchMapping(path = "/{metricId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MetricValueDto> updateMetricValue(@PathVariable("clusterId") UUID clusterId,
                                               @PathVariable("metricId") UUID metricId,
                                               @RequestBody ValueDto valueDto) {
        ClusterMetric updatedMetric = clusterMetricService.updateMetricValue(clusterId, metricId, valueDto.value());
        MetricValueDto dto = clusterMetricMapper.clusterMetricToDto(updatedMetric);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping(path = "/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable("clusterId") UUID clusterId,
                                          @PathVariable("metricId") UUID metricId) {
        clusterMetricService.deleteMetricValue(clusterId, metricId);
        return ResponseEntity.noContent().build();
    }
}
