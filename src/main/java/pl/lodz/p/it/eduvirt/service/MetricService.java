package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.UUID;

public interface MetricService {

    /* Create methods */

    void createNewMetric(String metricName, Metric.MetricCategory category);

    /* Read methods */

    Metric findById(UUID id);
    Page<Metric> findAllMetrics(int pageNumber, int pageSize);

    /* Delete methods */

    void deleteMetric(UUID metricId);
}
