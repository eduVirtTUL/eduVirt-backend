package pl.lodz.p.it.eduvirt.repository.ovirt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;

import java.util.UUID;

@Repository
@LoggerInterceptor
@RequiredArgsConstructor
public class ClusterRepository {

    @PersistenceContext(unitName = "oVirtPU")
    private final EntityManager entityManager;

    public boolean existsById(UUID clusterId) {
        String query = "SELECT COUNT(1) FROM cluster WHERE cluster_id = :clusterId";
        Object result = entityManager.createNativeQuery(query)
                .setParameter("clusterId", clusterId)
                .getSingleResult();
        return ((Number) result).intValue() > 0;
    }
}
