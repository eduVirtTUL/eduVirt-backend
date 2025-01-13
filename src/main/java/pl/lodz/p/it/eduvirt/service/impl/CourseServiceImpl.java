package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
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
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Override
    public Page<Course> getCourses(int page, int size) {
        return courseRepository.findAll(PageRequest.of(page, size));
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
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("Course not found"));
    }

    @Override
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }

    @Transactional
    @Override
    public void addResourceGroupToCourse(UUID courseId, ResourceGroup resourceGroup) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found"));
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
        courseRepository.deleteById(courseId);
    }

    @Override
    @Transactional
    public List<User> getTeachersForCourse(UUID courseId) {
        return courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found")).getTeachers();
    }

    @Override
    @Transactional
    public void addTeacherToCourse(UUID courseId, String email) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException("Course not found"));
        User teacher = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (teacher.getRoles().contains("/teacher")){
            if (!course.getTeachers().contains(teacher)){
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

        if (teacher.getRoles().contains("/teacher")){
            if (course.getTeachers().contains(teacher)){
                if (course.getTeachers().size() > 1){
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
}
