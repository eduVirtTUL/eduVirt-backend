package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceGroupPoolRepository extends JpaRepository<ResourceGroupPool, UUID>, JpaSpecificationExecutor<ResourceGroupPool> {
    Optional<ResourceGroupPool> getResourceGroupPoolByResourceGroupsContaining(ResourceGroup resourceGroup);

    List<ResourceGroupPool> getByCourseId(UUID courseId);

    ResourceGroupPool findByResourceGroupsContaining(ResourceGroup resourceGroup);

    Page<ResourceGroupPool> findAllByCourseTeachersContaining(User user, Specification<ResourceGroupPool> specification, Pageable pageable);

    Page<ResourceGroupPool> findAllByCourseTeachersContaining(User user, Pageable pageable);
}
