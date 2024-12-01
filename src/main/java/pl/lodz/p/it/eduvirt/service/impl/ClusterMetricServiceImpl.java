package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueAlreadyDefined;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueNotDefinedException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.MetricRepository;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;

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
                .orElseThrow(MetricNotFoundException::new);

        clusterMetricRepository.findByClusterIdAndMetric(clusterId, metric)
                .ifPresent(metricValue -> {
            throw new MetricValueAlreadyDefined();
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
    public ClusterMetric updateMetricValue(Cluster cluster, UUID metricId, double newValue) {
        UUID clusterId = UUID.fromString(cluster.id());
        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(MetricNotFoundException::new);

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(MetricValueNotDefinedException::new);

        metricValue.setValue(newValue);
        return clusterMetricRepository.saveAndFlush(metricValue);
    }

    @Override
    public void deleteMetricValue(Cluster cluster, UUID metricId) {
        UUID clusterId = UUID.fromString(cluster.id());

        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(MetricNotFoundException::new);

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(MetricValueNotDefinedException::new);

        clusterMetricRepository.delete(metricValue);
    }
}
