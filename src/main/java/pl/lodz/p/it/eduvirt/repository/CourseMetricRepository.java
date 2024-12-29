package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.CourseMetric;
import pl.lodz.p.it.eduvirt.entity.CourseMetricKey;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseMetricRepository extends JpaRepository<CourseMetric, CourseMetricKey> {

    List<CourseMetric> findAllByCourse(Course course);
    List<CourseMetric> findAllByCourseId(UUID courseId);
}
