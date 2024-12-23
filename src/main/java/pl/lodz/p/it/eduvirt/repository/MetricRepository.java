package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.general.Metric;

import java.util.UUID;

@Repository
@LoggerInterceptor
public interface MetricRepository extends JpaRepository<Metric, UUID> {}
