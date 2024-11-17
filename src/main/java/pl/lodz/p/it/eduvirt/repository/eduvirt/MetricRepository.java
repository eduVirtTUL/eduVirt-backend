package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.general.Metric;

import java.util.UUID;

@Repository
@LoggerInterceptor
public interface MetricRepository extends JpaRepository<Metric, UUID> {}
