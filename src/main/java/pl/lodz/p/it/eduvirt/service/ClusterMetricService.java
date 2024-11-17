package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;

import java.util.UUID;

public interface ClusterMetricService {

    void createNewValueForMetric(UUID clusterId, UUID metricId, double value);

    Page<ClusterMetric> findAllMetricValuesForCluster(UUID clusterId, Pageable pageable);

    ClusterMetric updateMetricValue(UUID clusterId, UUID metricId, double newValue);

    void deleteMetricValue(UUID clusterId, UUID metricId);
}
