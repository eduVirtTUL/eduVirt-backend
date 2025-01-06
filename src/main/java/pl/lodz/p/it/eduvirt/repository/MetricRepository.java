package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Metric;

import java.util.UUID;

@Repository
@LoggerInterceptor
@Transactional(propagation = Propagation.MANDATORY)
public interface MetricRepository extends JpaRepository<Metric, UUID> {}
