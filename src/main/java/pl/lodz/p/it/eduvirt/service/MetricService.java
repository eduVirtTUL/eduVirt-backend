package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;

import java.util.UUID;

public interface MetricService {

    void createNewMetric(String metricName);

    Page<Metric> findAllMetrics(int pageNumber, int pageSize);

    void deleteMetric(UUID metricId);
}
