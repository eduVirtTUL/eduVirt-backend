package pl.lodz.p.it.eduvirt.service.priviliges.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrivilegesServiceImpl implements PrivilegesService {

    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final PodStatefulRepository podStatefulRepository;
    private final PodStatelessRepository podStatelessRepository;

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

    private Course getCourseForResourceGroup(ResourceGroup resourceGroup) {
        if (resourceGroup.isStateless()) return resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup).getCourse();
        else return courseRepository.findByStateFulResourceGroupsContaining(resourceGroup);
    }

    private List<User> getUsersHavingAccessToResourceGroup(ResourceGroup resourceGroup) {
        if (resourceGroup.isStateless()) {
            ResourceGroupPool resourceGroupPool = resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup);
            List<PodStateless> statelessPods = podStatelessRepository.findByResourceGroupPoolId(resourceGroupPool.getId());
            return statelessPods.stream().map(PodStateless::getTeam).map(Team::getUsers).flatMap(Collection::stream).toList();
        }
        else {
            List<PodStateful> statefulPods = podStatefulRepository.findByResourceGroupId(resourceGroup.getId());
            return statefulPods.stream().map(PodStateful::getTeam).map(Team::getUsers).flatMap(Collection::stream).toList();
        }
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

        return isOwner || user.getRoles().contains(RoleConstants.ADMINISTRATOR);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateResourceGroupOwnershipOrAdminOrStudentWithAccess(ResourceGroup resourceGroup) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course rgCourse = getCourseForResourceGroup(resourceGroup);

        boolean adminAccess = user.getRoles().contains(RoleConstants.ADMINISTRATOR);
        boolean teacherAccess = rgCourse.getTeachers().stream().anyMatch(teacher -> teacher.getId().equals(userId));
        boolean studentAccess = getUsersHavingAccessToResourceGroup(resourceGroup).stream().map(User::getId).toList().contains(userId);

        return adminAccess || teacherAccess || studentAccess;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateCourseOwnership(Course course) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        return courseRepository.existsCourseForTeacher(course.getId(), userId);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateCourseOwnershipOrAdmin(Course course) {
        UUID userId = getUserId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        boolean isAdmin = user.getRoles().contains(RoleConstants.ADMINISTRATOR);
        if (!isAdmin) {
            return courseRepository.existsCourseForTeacher(course.getId(), userId);
        }

        return true;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public boolean validateCourseMembershipOwnershipOrAdmin(Course course) {
        UUID userId = getUserId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        List<String> roles = user.getRoles();

        List<User> courseTeachers = course.getTeachers();
        List<User> courseStudents = course.getTeams().stream().map(Team::getUsers).flatMap(Collection::stream).toList();

        boolean adminAccess = roles.contains(RoleConstants.ADMINISTRATOR);
        boolean teacherAccess = roles.contains(RoleConstants.TEACHER) && courseTeachers.stream().map(User::getId).toList().contains(userId);
        boolean studentAccess = roles.contains(RoleConstants.STUDENT) && courseStudents.contains(user);

        return adminAccess || teacherAccess || studentAccess;
    }

    private UUID getUserId() {
        return UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
