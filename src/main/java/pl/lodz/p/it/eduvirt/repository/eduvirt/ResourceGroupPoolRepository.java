package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceGroupPoolRepository extends JpaRepository<ResourceGroupPool, UUID> {
    Optional<ResourceGroupPool> getResourceGroupPoolByResourceGroupsContaining(ResourceGroup resourceGroup);
    List<ResourceGroupPool> getByCourseId(UUID courseId);
}
