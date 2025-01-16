package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.UserNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupPoolNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolConflictException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ResourceGroupPool addResourceGroupPool(ResourceGroupPool resourceGroupPool, UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course course = courseRepository.findByIdAndTeachersContaining(courseId, user).orElseThrow(() -> new CourseNotFoundException(courseId));

        boolean nameTaken = course.getResourceGroupPools().stream().anyMatch(rgp -> rgp.getName().equals(resourceGroupPool.getName()));
        if (nameTaken) {
            throw new ResourceGroupPoolAlreadyExistsException(resourceGroupPool.getName());
        }

        resourceGroupPool.setCourse(course);

        return resourceGroupPoolRepository.save(resourceGroupPool);
    }

    @Override
    public Page<ResourceGroupPool> getResourceGroupPools(Specification<ResourceGroupPool> spec, int page, int size) {
        return resourceGroupPoolRepository.findAll(spec, PageRequest.of(page, size, Sort.by("course.name").ascending()
                .and(Sort.by("name").ascending())));
    }

    @Override
    @Transactional
    public List<ResourceGroupPool> getResourceGroupPoolsByCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        checkTeacherOrAdministratorInCourse(courseId);
        return course.getResourceGroupPools();
    }

    @Override
    @Transactional
    public ResourceGroupPool getResourceGroupPool(UUID id) {
        ResourceGroupPool pool = resourceGroupPoolRepository.findById(id).orElseThrow(() -> new ResourceGroupPoolNotFoundException(id));
        checkTeacherOrAdministratorInCourse(pool.getCourse().getId());
        return pool;
    }

    @Override
    @Transactional
    public void addResourceGroupToPool(UUID poolId, ResourceGroup resourceGroup) {
        ResourceGroupPool pool = resourceGroupPoolRepository.findById(poolId).orElseThrow(() -> new ResourceGroupPoolNotFoundException(poolId));
        checkTeacherIsInCourse(pool.getCourse().getId());

        resourceGroup.setDescription(pool.getDescription());
        resourceGroup.setMaxRentTime(pool.getMaxRentTime());
        resourceGroup.setStateless(true);
        pool.getResourceGroups().add(resourceGroup);
        resourceGroupPoolRepository.save(pool);
    }

    @Override
    public void deleteResourceGroupPool(UUID id) {
        ResourceGroupPool pool = resourceGroupPoolRepository.findById(id).orElseThrow(() -> new ResourceGroupPoolNotFoundException(id));
        checkTeacherIsInCourse(pool.getCourse().getId());
        resourceGroupPoolRepository.delete(pool);
    }

    @Override
    @Transactional
    public ResourceGroupPool updateResourceGroupPool(UUID id, ResourceGroupPool resourceGroupPool, String ifMatch) {
        ResourceGroupPool pool = resourceGroupPoolRepository
                .findById(id)
                .orElseThrow(() -> new ResourceGroupPoolNotFoundException(resourceGroupPool.getId()));
        checkTeacherIsInCourse(pool.getCourse().getId());

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

    private void checkTeacherIsInCourse(UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        boolean exists = courseRepository.existsCourseForTeacher(courseId, userId);
        if (!exists) {
            throw new CourseNotFoundException(courseId);
        }
    }

    private void checkTeacherOrAdministratorInCourse(UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        boolean isAdmin = user.getRoles().contains("administrator");
        if (!isAdmin) {
            boolean exists = courseRepository.existsCourseForTeacher(courseId, userId);
            if (!exists) {
                throw new CourseNotFoundException(courseId);
            }
        }
    }
}
