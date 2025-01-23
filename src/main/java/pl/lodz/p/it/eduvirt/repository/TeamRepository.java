package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ovirt.engine.sdk4.types.User;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {

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

    @Query(value = "SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.users",
            countQuery = "SELECT COUNT(DISTINCT t) FROM Team t")
    Page<Team> findAllWithUsers(Pageable pageable);

    void deleteAllByCourseId(UUID courseId);

    @Query("SELECT DISTINCT u FROM Team t " +
            "JOIN t.users u " +
            "WHERE t.course.id = :courseId " +
            "AND t.course.courseType = 'SOLO'")
    List<User> findUsersInSoloCourse(@Param("courseId") UUID courseId);

    Page<Team> findByUsersIdAndNameContainingIgnoreCase(UUID userId, String search, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN t.users u WHERE t.course.id = :courseId AND " +
            "(:searchType = 'TEAM_NAME' AND LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            ":searchType = 'STUDENT_NAME' AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
            ":searchType = 'STUDENT_EMAIL' AND LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Team> findByCourseIdWithSearch(@Param("courseId") UUID courseId,
                                        @Param("search") String search,
                                        @Param("searchType") String searchType,
                                        Pageable pageable);

    @Query("SELECT DISTINCT t FROM Team t JOIN t.users u " +
            "WHERE t.course.id = :courseId AND " +
            "LOWER(SUBSTRING(u.email, 1, LOCATE('@', u.email) - 1)) IN :emailPrefixes")
    List<Team> findByCourseIdAndEmailPrefixes(
            @Param("courseId") UUID courseId,
            @Param("emailPrefixes") List<String> emailPrefixes,
            Sort sort);

    @Query("SELECT CAST(SUBSTRING(t.name, LENGTH(:prefix) + 1) AS integer) " +
            "FROM Team t " +
            "WHERE t.course.id = :courseId " +
            "AND t.name LIKE CONCAT(:prefix, '%') " +
            "ORDER BY CAST(SUBSTRING(t.name, LENGTH(:prefix) + 1) AS integer)")
    List<Integer> findTeamNumbersByCourseIdAndPrefix(@Param("courseId") UUID courseId,
                                                     @Param("prefix") String prefix);

    @Query("SELECT CAST(SUBSTRING(t.name, LENGTH(:prefix) + 1) AS integer) " +
            "FROM Team t " +
            "WHERE t.course.id = :courseId " +
            "AND t.name LIKE CONCAT(:prefix, '%') " +
            "ORDER BY CAST(SUBSTRING(t.name, LENGTH(:prefix) + 1) AS integer)")
    List<Integer> findTeamNumbersByPrefix(@Param("courseId") UUID courseId, @Param("prefix") String prefix);
}
