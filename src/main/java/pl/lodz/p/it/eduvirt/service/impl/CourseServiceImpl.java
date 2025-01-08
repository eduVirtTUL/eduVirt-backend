package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;

    @Override
    public Page<Course> getCourses(int page, int size) {
        return courseRepository.findAll(PageRequest.of(page, size));
    }

    @Override
    public List<Course> getCoursesForStudent(UUID studentId, Pageable pageable) {
        return courseRepository.findAllCoursesForStudent(studentId, pageable);
    }

    @Override
    public Course getCourse(UUID id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException("Course not found"));
    }

    @Override
    public Course addCourse(Course course) {
        return courseRepository.save(course);
    }
}
