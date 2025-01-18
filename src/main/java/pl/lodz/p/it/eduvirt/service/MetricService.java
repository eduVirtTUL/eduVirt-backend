package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.UUID;

public interface MetricService {

    /* Create methods */

    void createNewMetric(String metricName, Metric.MetricCategory category);

    /* Read methods */

    Metric findById(UUID id);
    Page<Metric> findAllMetrics(Pageable pageable);

    /* Delete methods */

    void deleteMetric(UUID metricId);
}
