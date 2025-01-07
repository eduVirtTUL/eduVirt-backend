package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    List<Course> getCourses();
    List<Course> getCoursesForStudent(UUID studentId, Pageable pageable);

    Course getCourse(UUID id);

    Course addCourse(Course course);
}
