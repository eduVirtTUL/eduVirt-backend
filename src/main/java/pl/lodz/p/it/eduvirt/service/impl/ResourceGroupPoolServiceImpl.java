package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupPoolNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolConflictException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceGroupPoolServiceImpl implements ResourceGroupPoolService {

    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final CourseRepository courseRepository;
    private final ETagHelper eTagHelper;

    @Override
    @Transactional
    public ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        boolean nameTaken = course.getResourceGroupPools().stream().anyMatch(rgp -> rgp.getName().equals(resourceGroupPool.getName()));
        if (nameTaken) {
            throw new ResourceGroupPoolAlreadyExistsException(resourceGroupPool.getName());
        }

        resourceGroupPool.setCourse(course);

        return resourceGroupPoolRepository.save(resourceGroupPool);
    }

    @Override
    public Page<ResourceGroupPool> getResourceGroupPools(int page, int size) {
        return resourceGroupPoolRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId) {
        return resourceGroupPoolRepository.getByCourseId(courseId);
    }

    @Override
    public ResourceGroupPool getResourceGroupPool(UUID id) {
        return resourceGroupPoolRepository.findById(id).orElseThrow(() -> new ResourceGroupPoolNotFoundException(id));
    }

    @Override
    @Transactional
    public void addResourceGroupToPool(UUID poolId, ResourceGroup resourceGroup) {
        ResourceGroupPool pool = resourceGroupPoolRepository.findById(poolId).orElseThrow(() -> new ResourceGroupPoolNotFoundException(poolId));
        resourceGroup.setDescription(pool.getDescription());
        resourceGroup.setMaxRentTime(pool.getMaxRentTime());
        resourceGroup.setStateless(true);
        pool.getResourceGroups().add(resourceGroup);
        resourceGroupPoolRepository.save(pool);
    }

    @Override
    public void deleteResourceGroupPool(UUID id) {
        ResourceGroupPool pool = resourceGroupPoolRepository.findById(id).orElseThrow(() -> new ResourceGroupPoolNotFoundException(id));
        resourceGroupPoolRepository.delete(pool);
    }

    @Override
    @Transactional
    public ResourceGroupPool updateResourceGroupPool(UUID id, ResourceGroupPool resourceGroupPool, String ifMatch) {
        ResourceGroupPool pool = resourceGroupPoolRepository
                .findById(id)
                .orElseThrow(() -> new ResourceGroupPoolNotFoundException(resourceGroupPool.getId()));

        if (!eTagHelper.validateEtag(ifMatch, pool)) {
            throw new ResourceGroupPoolConflictException();
        }

        pool.getResourceGroups()
                .forEach(resourceGroup -> {
                    resourceGroup.setDescription(resourceGroupPool.getDescription());
                    resourceGroup.setMaxRentTime(resourceGroupPool.getMaxRentTime());
                });

        pool.setName(resourceGroupPool.getName());
        pool.setDescription(resourceGroupPool.getDescription());
        pool.setMaxRentTime(resourceGroupPool.getMaxRentTime());
        pool.setGracePeriod(resourceGroupPool.getGracePeriod());
        pool.setMaxRent(resourceGroupPool.getMaxRent());

        return resourceGroupPoolRepository.save(pool);

    }
}
