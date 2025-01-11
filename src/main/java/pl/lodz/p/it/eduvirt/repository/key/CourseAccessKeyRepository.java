package pl.lodz.p.it.eduvirt.repository.key;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;

import java.util.Optional;
import java.util.UUID;

public interface CourseAccessKeyRepository extends JpaRepository<CourseAccessKey, UUID> {
    Optional<CourseAccessKey> findByKeyValue(String keyValue);
    Optional<CourseAccessKey> findByCourseId(UUID courseId);
    boolean existsByCourseId(UUID courseId);
    boolean existsByKeyValue(String keyValue);
}
