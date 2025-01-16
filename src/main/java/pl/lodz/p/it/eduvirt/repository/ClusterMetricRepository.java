package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.ClusterMetric;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@LoggerInterceptor
@Transactional(propagation = Propagation.MANDATORY)
public interface ClusterMetricRepository extends JpaRepository<ClusterMetric, UUID> {

    @PreAuthorize("hasAuthority('administrator')")
    Optional<ClusterMetric> findByClusterIdAndMetric(UUID clusterId, Metric metric);

    @PreAuthorize("hasAuthority('administrator')")
    Page<ClusterMetric> findAllByClusterId(UUID clusterId, Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    List<ClusterMetric> findAllByClusterId(UUID clusterId);

    void deleteByMetric(Metric metric);
}
