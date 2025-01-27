package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClusterMetricService {

    /* Create methods */

    void createNewValueForMetric(Cluster cluster, UUID metricId, double value);

    /* Read methods */

    Optional<ClusterMetric> findClusterMetricByClusterAndMetric(Cluster cluster, Metric metric);
    Page<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster, Pageable pageable);
    List<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster);

    /* Update methods */

    ClusterMetric updateMetricValue(UUID clusterMetricId, ClusterMetric clusterMetric, String ifMatch);

    /* Delete methods */

    void deleteMetricValue(Cluster cluster, UUID metricId);
}
