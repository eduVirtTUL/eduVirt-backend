package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.ClusterMetric;

import java.util.Optional;
import java.util.UUID;

@Repository
@LoggerInterceptor
public interface ClusterMetricRepository extends JpaRepository<ClusterMetric, UUID> {

    Optional<ClusterMetric> findByClusterIdAndMetric(UUID clusterId, Metric metric);
    Page<ClusterMetric> findAllByClusterId(UUID clusterId, Pageable pageable);
}
