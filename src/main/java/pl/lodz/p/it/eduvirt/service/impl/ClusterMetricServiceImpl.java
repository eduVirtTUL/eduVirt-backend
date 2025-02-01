package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricConflictException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricExistsException;
import pl.lodz.p.it.eduvirt.exceptions.ClusterMetricNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.MetricNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ClusterMetricRepository;
import pl.lodz.p.it.eduvirt.repository.MetricRepository;
import pl.lodz.p.it.eduvirt.service.ClusterMetricService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class ClusterMetricServiceImpl implements ClusterMetricService {

    /* Repositories */

    private final MetricRepository metricRepository;
    private final ClusterMetricRepository clusterMetricRepository;

    /* Util */

    private final ETagHelper eTagHelper;

    /* Create methods */

    @PreAuthorize("hasAuthority('administrator')")
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

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public Optional<ClusterMetric> findClusterMetricByClusterAndMetric(Cluster cluster, Metric metric) {
        UUID clusterId = UUID.fromString(cluster.id());
        return clusterMetricRepository.findByClusterIdAndMetric(clusterId, metric);
    }

    @PreAuthorize("hasAuthority('administrator')")
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

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public ClusterMetric updateMetricValue(UUID clusterMetricId, ClusterMetric clusterMetric, String ifMatch) {
        ClusterMetric metricValue = clusterMetricRepository.findById(clusterMetricId)
                .orElseThrow(() -> new ClusterMetricNotFoundException(
                        "Cluster metric value %s could not be found in the database!".formatted(clusterMetricId)));

        if (!eTagHelper.validateEtag(ifMatch, metricValue))
            throw new ClusterMetricConflictException();

        metricValue.setValue(clusterMetric.getValue());
        return clusterMetricRepository.saveAndFlush(metricValue);
    }

    /* Delete methods */

    @PreAuthorize("hasAuthority('administrator')")
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
