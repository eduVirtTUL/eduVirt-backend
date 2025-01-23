package pl.lodz.p.it.eduvirt.service.priviliges.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivilegesServiceImpl implements PrivilegesService {
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    private boolean isResourceGroupOwner(ResourceGroup resourceGroup, UUID userId) {
        Course course;
        if (resourceGroup.isStateless()) {
            course = resourceGroupPoolRepository
                    .findByResourceGroupsContaining(resourceGroup)
                    .getCourse();
        } else {
            course = courseRepository.findByStateFulResourceGroupsContaining(resourceGroup);
        }
        return courseRepository.existsCourseForTeacher(course.getId(), userId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateResourceGroupOwnership(ResourceGroup resourceGroup) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());

        return isResourceGroupOwner(resourceGroup, userId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateResourceGroupOwnershipOrAdmin(ResourceGroup resourceGroup) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

        boolean isOwner = isResourceGroupOwner(resourceGroup, userId);

        return isOwner || user.getRoles().contains("administrator");
    }
}
