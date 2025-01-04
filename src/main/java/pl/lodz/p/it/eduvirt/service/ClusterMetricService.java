package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;

import java.util.List;
import java.util.UUID;

public interface ClusterMetricService {

    void createNewValueForMetric(Cluster cluster, UUID metricId, double value);

    Page<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster, Pageable pageable);
    List<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster);

    ClusterMetric updateMetricValue(Cluster cluster, UUID metricId, double newValue);

    void deleteMetricValue(Cluster cluster, UUID metricId);
}
