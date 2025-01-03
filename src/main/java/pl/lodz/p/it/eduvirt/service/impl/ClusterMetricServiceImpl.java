package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.general.Metric;
import pl.lodz.p.it.eduvirt.entity.reservation.ClusterMetric;
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
public class ClusterMetricServiceImpl implements ClusterMetricService {

    private final MetricRepository metricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

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

    @Override
    public Page<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster, Pageable pageable) {
        UUID clusterId = UUID.fromString(cluster.id());
        return clusterMetricRepository.findAllByClusterId(clusterId, pageable);
    }

    @Override
    public List<ClusterMetric> findAllMetricValuesForCluster(Cluster cluster) {
        UUID clusterId = UUID.fromString(cluster.id());
        return clusterMetricRepository.findAllByClusterId(clusterId);
    }

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
