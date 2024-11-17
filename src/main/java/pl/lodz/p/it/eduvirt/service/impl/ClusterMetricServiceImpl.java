package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueAlreadyDefined;
import pl.lodz.p.it.eduvirt.exceptions.metric.MetricValueNotDefinedException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.MetricRepository;
import pl.lodz.p.it.eduvirt.repository.ovirt.ClusterRepository;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;

import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class ClusterMetricServiceImpl implements ClusterMetricService {

    private final MetricRepository metricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    private final ClusterRepository clusterRepository;

    @Override
    public void createNewValueForMetric(UUID clusterId, UUID metricId, double value) {
        if (!clusterRepository.existsById(clusterId)) throw new ClusterNotFoundException();

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
    public Page<ClusterMetric> findAllMetricValuesForCluster(UUID clusterId, Pageable pageable) {
        if (!clusterRepository.existsById(clusterId)) throw new ClusterNotFoundException();
        return clusterMetricRepository.findAllByClusterId(clusterId, pageable);
    }

    @Override
    public ClusterMetric updateMetricValue(UUID clusterId, UUID metricId, double newValue) {
        if (!clusterRepository.existsById(clusterId)) throw new ClusterNotFoundException();

        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(MetricNotFoundException::new);

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(MetricValueNotDefinedException::new);

        metricValue.setValue(newValue);
        return clusterMetricRepository.saveAndFlush(metricValue);
    }

    @Override
    public void deleteMetricValue(UUID clusterId, UUID metricId) {
        if (!clusterRepository.existsById(clusterId)) throw new ClusterNotFoundException();

        Metric metric = metricRepository.findById(metricId)
                .orElseThrow(MetricNotFoundException::new);

        ClusterMetric metricValue = clusterMetricRepository
                .findByClusterIdAndMetric(clusterId, metric)
                .orElseThrow(MetricValueNotDefinedException::new);

        clusterMetricRepository.delete(metricValue);
    }
}
