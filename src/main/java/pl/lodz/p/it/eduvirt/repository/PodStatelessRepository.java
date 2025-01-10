package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.PodStateless;

import java.util.List;
import java.util.UUID;

@Repository
public interface PodStatelessRepository extends JpaRepository<PodStateless, UUID> {
    List<PodStateless> findByTeamId(UUID teamId);
    List<PodStateless> findByCourseId(UUID courseId);
    List<PodStateless> findByResourceGroupPoolId(UUID resourceGroupPoolId);
    boolean existsByResourceGroupPoolId(UUID resourceGroupPoolId);
}