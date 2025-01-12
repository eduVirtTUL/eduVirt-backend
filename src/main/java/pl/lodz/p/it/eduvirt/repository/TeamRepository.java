package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    List<Team> findByUsersContains(UUID userId);
    Page<Team> findByUsersContains(UUID userId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.course.id = :courseId")
    List<Team> findByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT t FROM Team t WHERE t.course.id = :courseId")
    Page<Team> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t " +
            "WHERE :userId MEMBER OF t.users " +
            "AND t.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT t FROM Team t WHERE :user MEMBER OF t.users AND t.course = :course")
    Optional<Team> findByUserIdAndCourse(@Param("user") UUID userId, @Param("course") Course course);

    boolean existsByNameAndCourseId(String name, UUID courseId);

    Long countByCourseId(UUID courseId);

}
