package pl.lodz.p.it.eduvirt.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;

import java.util.List;
import java.util.UUID;

public interface CourseService {
    Page<Course> getCourses(int page, int size);

    List<Course> getCourses();

    List<Course> getCoursesForStudent(UUID studentId, Pageable pageable);

    Course getCourse(UUID id);

    Course addCourse(Course course);

    void addResourceGroupToCourse(UUID courseId, ResourceGroup resourceGroup);

    List<ResourceGroup> getStateFullResourceGroups(UUID courseId);

    void deleteCourse(UUID courseId);
}
