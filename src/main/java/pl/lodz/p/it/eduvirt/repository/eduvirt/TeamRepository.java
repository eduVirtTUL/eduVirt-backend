package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

    Optional<Team> findByKey(String key);

    List<Team> findByUsersContains(UUID userId);

    @Query("SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.course c " +
            "LEFT JOIN FETCH t.users " +
            "WHERE c.id = :courseId")
    List<Team> findByCourses_IdWithFetch(@Param("courseId") UUID courseId);

    boolean existsByKey(String key);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t " +
            "WHERE :userId MEMBER OF t.users " +
            "AND t.course.id = :courseId")
    boolean existsByUserIdAndCourseId(@Param("userId") UUID userId, @Param("courseId") UUID courseId);

    
}

