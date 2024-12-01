package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.MetricRepository;
import pl.lodz.p.it.eduvirt.service.MetricService;

import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricRepository metricRepository;

    @Override
    public void createNewMetric(String metricName) {
        String actualMetricName = metricName.toLowerCase().replaceAll("\\s+", "_");
        Metric newMetric = new Metric(actualMetricName);
        metricRepository.saveAndFlush(newMetric);
    }

    @Override
    public Page<Metric> findAllMetrics(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));
            return metricRepository.findAll(pageable);
        } catch (IllegalArgumentException exception) {
            return Page.empty();
        }
    }

    @Override
    public void deleteMetric(UUID metricId) {
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(MetricNotFoundException::new);
        metricRepository.delete(metric);
    }
}
