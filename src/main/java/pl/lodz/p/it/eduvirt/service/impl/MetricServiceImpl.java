package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.CourseMetricRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.MetricService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class MetricServiceImpl implements MetricService {

    /* Repositories */

    private final MetricRepository metricRepository;
    private final CourseMetricRepository courseMetricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    /* Create methods */

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public void createNewMetric(String metricName, Metric.MetricCategory category) {
        Metric newMetric = new Metric(metricName, category);
        metricRepository.saveAndFlush(newMetric);
    }

    /* Read methods */

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public Metric findById(UUID id) {
        return metricRepository.findById(id)
                .orElseThrow(() -> new MetricNotFoundException(id));
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public Page<Metric> findAllMetrics(Pageable pageable) {
        return metricRepository.findAll(pageable);
    }

    /* Update methods */

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public void deleteMetric(UUID metricId) {
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new MetricNotFoundException(metricId));

        /* Fetch all course metrics */
        courseMetricRepository.deleteByMetric(metric);

        /* Fetch all cluster metrics */
        clusterMetricRepository.deleteByMetric(metric);

        /* Delete metric after the values are removed */
        metricRepository.delete(metric);
    }
}
