package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.metric.CreateMetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.MetricValueDto;
import pl.lodz.p.it.eduvirt.dto.metric.UpdateMetricValueDto;
import pl.lodz.p.it.eduvirt.entity.CourseMetric;
import pl.lodz.p.it.eduvirt.service.CourseMetricService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/course/{courseId}/metric")
@RequiredArgsConstructor
public class CourseMetricController {
    private final CourseMetricService courseMetricService;

    @PostMapping
    public ResponseEntity<Void> createMetric(@PathVariable UUID courseId, @RequestBody CreateMetricValueDto createMetricValueDto) {
        courseMetricService.addMetricToCourse(courseId, createMetricValueDto.metricId(), createMetricValueDto.value());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<MetricValueDto>> getMetrics(@PathVariable UUID courseId) {
        List<CourseMetric> metrics = courseMetricService.getCourseMetrics(courseId);
        return ResponseEntity.ok(metrics.stream().map(metric
                -> new MetricValueDto(
                metric.getMetric().getId(),
                metric.getMetric().getName(),
                metric.getValue())).toList()
        );
    }

    @GetMapping("/{metricId}")
    public ResponseEntity<MetricValueDto> getMetric(@PathVariable UUID courseId, @PathVariable UUID metricId) {
        CourseMetric metric = courseMetricService.getCourseMetric(courseId, metricId);
        return ResponseEntity.ok(new MetricValueDto(metric.getMetric().getId(), metric.getMetric().getName(), metric.getValue()));
    }

    @DeleteMapping("/{metricId}")
    public ResponseEntity<Void> deleteMetric(@PathVariable UUID courseId, @PathVariable UUID metricId) {
        courseMetricService.removeMetricFromCourse(courseId, metricId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{metricId}")
    public ResponseEntity<Void> updateMetric(@PathVariable UUID courseId, @PathVariable UUID metricId, @RequestBody UpdateMetricValueDto updateMetricValueDto) {
        courseMetricService.updateCourseMetric(courseId, metricId, updateMetricValueDto.value());
        return ResponseEntity.ok().build();
    }
}
