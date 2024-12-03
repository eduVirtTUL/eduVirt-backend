package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.ResourceGroupPoolNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.service.ResourceGroupPoolService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceGroupPoolServiceImpl implements ResourceGroupPoolService {

    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final CourseRepository courseRepository;

    @Override
    public ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        resourceGroupPool.setCourse(course);

        return resourceGroupPoolRepository.save(resourceGroupPool);
    }

    @Override
    public List<ResourceGroupPool> getResourceGroupPools() {
        return resourceGroupPoolRepository.findAll();
    }

    @Override
    public List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId) {
        return resourceGroupPoolRepository.getByCourseId(courseId);
    }

    @Override
    public ResourceGroupPool getResourceGroupPool(UUID id) {
        return resourceGroupPoolRepository.findById(id).orElseThrow(() -> new ResourceGroupPoolNotFoundException(id));
    }
}
