package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.UserNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseAlreadyExists;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseConflictException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.List;
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
    public Page<Course> getCourses(int page, int size) {
        return courseRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public Page<Course> getCourses(int page, int size, String search) {
        return courseRepository.findAllByNameContainingIgnoreCase(search, PageRequest.of(page, size));
    }

    @Override
    public List<Course> getCourses() {
        return courseRepository.findAll();
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
    public Course addCourse(Course course, String teacherEmail) {
    User teacher = userRepository.findByEmailIgnoreCase(teacherEmail)
            .orElseThrow(() -> new UserNotFoundException("Teacher not found"));

    if (!teacher.getRoles().contains("/teacher")) {
        throw new IllegalArgumentException("User is not a teacher");
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
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        resourceGroup.setStateless(false);
        course.getStateFullResourceGroups().add(resourceGroup);
        courseRepository.save(course);
    }

    @Transactional
    @Override
    public List<ResourceGroup> getStateFullResourceGroups(UUID courseId) {
        return courseRepository
                .findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getStateFullResourceGroups();
    }

    @Override
    @Transactional
    public void deleteCourse(UUID courseId) {
        podStatelessRepository.deleteAllByCourseId(courseId);
        podStatefulRepository.deleteAllByCourseId(courseId);
        courseAccessKeyRepository.deleteByCourseId(courseId);
        courseRepository.deleteById(courseId);
    }

    @Override
    @Transactional
    public List<User> getTeachersForCourse(UUID courseId) {
        return courseRepository.findByIdWithTeachers(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeachers();
    }

    @Override
    @Transactional
    public void addTeacherToCourse(UUID courseId, String email) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        User teacher = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (teacher.getRoles().contains("/teacher")) {
            if (!course.getTeachers().contains(teacher)) {
                course.getTeachers().add(teacher);
                courseRepository.saveAndFlush(course);
            } else {
                throw new IllegalArgumentException("This teacher is already assigned to this course");
            }
        } else {
            throw new IllegalArgumentException("User is not a teacher");
        }
    }

    @Override
    @Transactional
    public void removeTeacherFromCourse(UUID courseId, String email) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found"));
        User teacher = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (teacher.getRoles().contains("/teacher")) {
            if (course.getTeachers().contains(teacher)) {
                if (course.getTeachers().size() > 1) {
                    course.getTeachers().remove(teacher);
                    courseRepository.saveAndFlush(course);
                } else {
                    throw new IllegalArgumentException("Course must have at least one teacher");
                }
            } else {
                throw new IllegalArgumentException("This teacher is not assigned to this course");
            }
        } else {
            throw new IllegalArgumentException("User is not a teacher");
        }
    }

    @Override
    @Transactional
    public Course updateCourse(UUID courseId, Course course, String etag) {
        Course existingCourse = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

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
