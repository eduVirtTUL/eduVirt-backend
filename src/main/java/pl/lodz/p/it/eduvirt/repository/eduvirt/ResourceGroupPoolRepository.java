package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResourceGroupPoolRepository extends JpaRepository<ResourceGroupPool, UUID> {
    List<ResourceGroupPool> getByCourseId(UUID courseId);
}
