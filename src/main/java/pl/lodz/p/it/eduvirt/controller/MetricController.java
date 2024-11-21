package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.mappers.MetricMapper;
import pl.lodz.p.it.eduvirt.service.MetricService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/metrics")
public class MetricController {

    private final MetricService metricService;

    private final MetricMapper metricMapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createNewMetric(@RequestBody CreateMetricDto createDto) {
        metricService.createNewMetric(createDto.name());
        return ResponseEntity.noContent().build();
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PageDto<MetricDto>> getAllMetrics(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                    @RequestParam(name = "pageSize", defaultValue = "10", required = false) int pageSize) {
        Page<Metric> metricPage = metricService.findAllMetrics(pageNumber, pageSize);
        PageDto<MetricDto> listOfDTOs = new PageDto<>(
                metricPage.getContent().stream().map(metricMapper::metricToDto).toList(),
                new PageInfoDto(metricPage.getNumber(), metricPage.getNumberOfElements(),
                        metricPage.getTotalPages(), metricPage.getTotalElements())
        );

        if (listOfDTOs.items().isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @DeleteMapping(path = "/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable UUID metricId) {
        metricService.deleteMetric(metricId);
        return ResponseEntity.noContent().build();
    }
}
