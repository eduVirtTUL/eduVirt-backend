package pl.lodz.p.it.eduvirt.service;

import org.springframework.data.domain.Page;
import pl.lodz.p.it.eduvirt.entity.Course;

import java.util.UUID;

public interface CourseService {
    Page<Course> getCourses(int page, int size);

    Course getCourse(UUID id);

    Course addCourse(Course course);

}
