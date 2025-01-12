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

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users u WHERE u.id = :userId")
    List<Team> findByUsersId(@Param("userId") UUID userId);

    @Query(value = "SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users u WHERE u.id = :userId",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t JOIN t.users u WHERE u.id = :userId")
    Page<Team> findByUsersId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users WHERE t.course.id = :courseId")
    List<Team> findByCourseId(@Param("courseId") UUID courseId);
    
    @Query(value = "SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users WHERE t.course.id = :courseId",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t WHERE t.course.id = :courseId")
    Page<Team> findByCourseId(@Param("courseId") UUID courseId, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t " +
           "JOIN t.users u WHERE u.id = :userId AND t.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users u " +
           "WHERE u.id = :user AND t.course = :course")
    Optional<Team> findByUserIdAndCourse(@Param("user") UUID userId, @Param("course") Course course);

    boolean existsByNameAndCourseId(String name, UUID courseId);

    Long countByCourseId(UUID courseId);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users WHERE t.id = :id")
    Optional<Team> findByIdWithUsers(@Param("id") UUID id);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users")
    List<Team> findAllWithUsers();
    
    @Query(value = "SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users",
           countQuery = "SELECT COUNT(DISTINCT t) FROM Team t")
    Page<Team> findAllWithUsers(Pageable pageable);
}
