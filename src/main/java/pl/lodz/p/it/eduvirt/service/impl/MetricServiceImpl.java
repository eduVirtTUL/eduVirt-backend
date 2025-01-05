package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.MetricService;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class MetricServiceImpl implements MetricService {

    /* Repositories */

    private final MetricRepository metricRepository;

    /* Create methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public void createNewMetric(String metricName) {
        String actualMetricName = metricName.toLowerCase().replaceAll("\\s+", "_");
        Metric newMetric = new Metric(actualMetricName);
        metricRepository.saveAndFlush(newMetric);
    }

    /* Read methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public Page<Metric> findAllMetrics(int pageNumber, int pageSize) {
        try {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("name"));
            return metricRepository.findAll(pageable);
        } catch (IllegalArgumentException exception) {
            return Page.empty();
        }
    }

    /* Update methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public void deleteMetric(UUID metricId) {
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new MetricNotFoundException(metricId));
        metricRepository.deleteAllInBatch(List.of(metric));
    }
}
