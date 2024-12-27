package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Course;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    List<Course> getCourses();

    Course getCourse(UUID id);

    Course addCourse(Course course);

}
