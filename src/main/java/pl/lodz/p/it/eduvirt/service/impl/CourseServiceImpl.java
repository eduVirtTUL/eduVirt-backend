package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.course.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotAuthorizedException;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseAccessKeyRepository courseAccessKeyRepository;
    private final PodStatefulRepository podStatefulRepository;
    private final PodStatelessRepository podStatelessRepository;
    private final TeamRepository teamRepository;
    private final ETagHelper eTagHelper;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Override
    public Page<Course> getCourses(int page, int size, String search, String sortOrder) {
        Sort sort = null;
        if (Objects.equals(sortOrder, "ASC")) {
            sort = Sort.by("name").ascending();
        } else if ("DESC".equals(sortOrder)) {
            sort = Sort.by("name").descending();
        }
        if (Objects.equals(search, "")) {
            return courseRepository.findAll(PageRequest.of(page, size, sort));
        }

        return courseRepository.findAllByNameContainingIgnoreCase(search, PageRequest.of(page, size, sort));
    }

    @Override
    @Transactional
    public Page<Course> getCoursesForTeacher(UUID userId, int page, int size, String search) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (search != null) {
            return courseRepository.findAllByTeachersContainingAndNameContainingIgnoreCase(user, search, PageRequest.of(page, size));
        }
        return courseRepository.findAllByTeachersContaining(user, PageRequest.of(page, size));
    }

    @Override
    @Transactional
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    @Override
    public List<Course> getCourses(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        return courseRepository.findAllByTeachersContaining(user);
    }

    @Override
    public List<Course> getCoursesForStudent(User student, Pageable pageable) {
        return courseRepository.findAllCoursesForStudent(student, pageable);
    }

    @Override
    public Course getCourse(UUID id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Override
    @Transactional
    public Course getCourse(UUID id, UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getRoles().contains("administrator")) {
            return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
        }
        return courseRepository.findByIdAndTeachersContaining(id, user).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    @Transactional
    public Course addCourse(Course course, String teacherEmail) {
        User teacher = userRepository.findByEmailIgnoreCase(teacherEmail)
                .orElseThrow(() -> new UserNotFoundException("Teacher not found"));

        if (!teacher.getRoles().contains(RoleConstants.TEACHER)) {
            throw new UserNotAuthorizedException("User with id %s is not a teacher"
                    .formatted(teacher.getId()));
        }

        if (course.getTeachers() == null) {
            course.setTeachers(List.of(teacher));
        } else {
            course.getTeachers().add(teacher);
        }

        return courseRepository.saveAndFlush(course);
    }

    @Transactional
    @Override
    public void addResourceGroupToCourse(UUID courseId, ResourceGroup resourceGroup) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course course = courseRepository.findByIdAndTeachersContaining(courseId, user).orElseThrow(() -> new CourseNotFoundException(courseId));
        resourceGroup.setStateless(false);
        course.getStateFullResourceGroups().add(resourceGroup);
        courseRepository.save(course);
    }

    @Transactional
    @Override
    public List<ResourceGroup> getStateFullResourceGroups(UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course course;
        if (user.getRoles().contains("administrator")) {
            course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        } else {
            course = courseRepository.findByIdAndTeachersContaining(courseId, user).orElseThrow(() -> new CourseNotFoundException(courseId));
        }

        return course
                .getStateFullResourceGroups();
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course course = courseRepository.findByIdAndTeachersContaining(courseId, user)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
        podStatelessRepository.deleteAllByCourseId(course.getId());
        podStatefulRepository.deleteAllByCourseId(course.getId());
        courseAccessKeyRepository.deleteByCourseId(course.getId());
        courseRepository.deleteById(course.getId());
    }

    // * Teacher methods * //

    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public List<User> getTeachersForCourse(UUID courseId) {
        return courseRepository.findByIdWithTeachers(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeachers();
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public void addTeacherToCourse(Course course, String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("User with email %s not found".formatted(email)));

        if (user.getRoles().contains("/teacher")) {
            if (!course.getTeachers().contains(user)) {
                course.getTeachers().add(user);
                courseRepository.saveAndFlush(course);
            } else {
                throw new TeacherAlreadyInCourseException();
            }
        } else {
            throw new UserNotAuthorizedException("User with id %s is not a teacher".formatted(user.getId()));
        }
    }


    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public void removeTeacherFromCourse(Course course, String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("User with email %s not found".formatted(email)));

        if (user.getRoles().contains("/teacher")) {
            if (course.getTeachers().contains(user)) {
                if (course.getTeachers().size() > 1) {
                    course.getTeachers().remove(user);
                    courseRepository.saveAndFlush(course);
                } else {
                    throw new CourseNoTeachersException("Course with id %s cannot contain less than 1 teacher".formatted(course.getId()));
                }
            } else {
                throw new TeacherNotInCourseException();
            }
        } else {
            throw new UserNotAuthorizedException("User with id %s is not a teacher".formatted(user.getId()));
        }
    }

    @Override
    @Transactional
    public Course updateCourse(UUID courseId, Course course, String etag) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Course existingCourse;
        if (user.getRoles().contains("administrator")) {
            existingCourse = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        } else {
            existingCourse = courseRepository.findByIdAndTeachersContaining(courseId, user)
                    .orElseThrow(() -> new CourseNotFoundException(courseId));
        }

        if (!eTagHelper.validateEtag(etag, existingCourse)) {
            throw new CourseConflictException();
        }

        boolean isNameTaken = courseRepository.existsByIdNotAndName(existingCourse.getId(), course.getName());
        if (isNameTaken) {
            throw new CourseAlreadyExists(course.getName());
        }

        existingCourse.setName(course.getName());
        existingCourse.setDescription(course.getDescription());
        existingCourse.setExternalLink(course.getExternalLink());
        return courseRepository.save(existingCourse);
    }

    @Override
    @Transactional
    public void resetCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        teamRepository.deleteAllByCourseId(course.getId());
    }

    @Override
    @Transactional
    public Course getCourseByResourceGroup(ResourceGroup resourceGroup) {
        Course course;
        if (resourceGroup.isStateless()) {
            course = resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup).getCourse();
        } else {
            course = courseRepository.findByStateFullResourceGroupsContaining(resourceGroup);
        }

        return course;
    }
}
