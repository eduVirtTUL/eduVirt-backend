package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.PodStateful;

import java.util.List;
import java.util.UUID;

@Repository
public interface PodRepository extends JpaRepository<PodStateful, UUID> {
    List<PodStateful> findByTeamId(UUID teamId);
    List<PodStateful> findByCourseId(UUID courseId);
    List<PodStateful> findByResourceGroupId(UUID resourceGroupId);
    boolean existsByResourceGroupId(UUID resourceGroupId);
}