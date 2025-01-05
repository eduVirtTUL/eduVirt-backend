package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricExistsException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ClusterMetricServiceImpl implements ClusterMetricService {

    /* Repositories */

    private final MetricRepository metricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    /* Create methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public void createNewValueForMetric(Cluster cluster, UUID metricId, double value) {
        UUID clusterId = UUID.fromString(cluster.id());
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new MetricNotFoundException(metricId));

        clusterMetricRepository.findByClusterIdAndMetric(clusterId, metric)
                .ifPresent(metricValue -> {
            throw new ClusterMetricExistsException(clusterId, metricId);
        });

        ClusterMetric newMetricValue = new ClusterMetric(clusterId, metric, value);
        clusterMetricRepository.saveAndFlush(newMetricValue);
    }

    /* Read methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public Page<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster, Pageable pageable) {
        UUID clusterId = UUID.fromString(cluster.id());
        return clusterMetricRepository.findAllByClusterId(clusterId, pageable);
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster) {
        UUID clusterId = UUID.fromString(cluster.id());
        return clusterMetricRepository.findAllByClusterId(clusterId);
    }

    /* Update methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public ClusterMetric updateMetricValue(Cluster cluster, UUID metricId, double newValue) {
        UUID clusterId = UUID.fromString(cluster.id());
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new MetricNotFoundException(metricId));

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(() -> new ClusterMetricNotFoundException(clusterId, metricId));

        metricValue.setValue(newValue);
        return clusterMetricRepository.saveAndFlush(metricValue);
    }

    /* Delete methods */

    @PreAuthorize("hasRole('administrator')")
    @Override
    public void deleteMetricValue(Cluster cluster, UUID metricId) {
        UUID clusterId = UUID.fromString(cluster.id());

        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new MetricNotFoundException(metricId));

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(() -> new ClusterMetricNotFoundException(clusterId, metricId));

        clusterMetricRepository.delete(metricValue);
    }
}
