package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.CourseRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Course getCourse(UUID id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("Course not found"));
    }

    @Override
    public Course setCourseKey(UUID courseId, String key) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));
                
        if (key == null || key.length() < 4) {
            throw new IllegalArgumentException("Course key must be at least 4 characters");
        }
    
        if (!course.isTeamBased()) {
            course.setCourseKey("s" + key);
        } else {
            throw new IllegalStateException("Cannot set key for team-based course");
        }
    
        return courseRepository.save(course);
    }

    @Override
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }
}
