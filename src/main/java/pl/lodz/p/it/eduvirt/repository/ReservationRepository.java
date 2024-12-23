package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.reservation.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@LoggerInterceptor
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end")
    List<Reservation> findReservationForGivenPeriod(@Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);

    List<Reservation> findReservationsByResourceGroupAndTeam(
            ResourceGroup resourceGroup, Team team);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :resourceGroup ORDER BY r.endTime DESC LIMIT 1")
    Optional<Reservation> findLastReservation(@Param("resourceGroup") ResourceGroup resourceGroup,
                                              @Param("team") Team team);

    @Query("SELECT r FROM Reservation r WHERE r.startTime <= :probeTime AND r.endTime > :probeTime " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course = :course)")
    List<Reservation> findCurrentReservationsForCourse(
            @Param("course") Course course, @Param("probeTime") LocalDateTime probeTime);

//    // TODO: Uncomment when clusterId becomes a part of Course
//    @Query("SELECT r FROM Reservation r WHERE r.startTime <= :probeTime AND r.endTime > :probeTime " +
//            "AND r.team IN (SELECT t FROM Team t WHERE t.course.clusterId = :cluster)")
//    List<Reservation> findCurrentReservationsForCluster(
//            @Param("cluster") UUID clusterId, @Param("probeTime") LocalDateTime probeTime);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > current_timestamp() " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course = :course)")
    List<Reservation> findAllActiveReservationsForCourse(@Param("course") Course course);

//    // TODO: Uncomment when clusterId becomes a part of Course
//    @Query("SELECT r FROM Reservation r WHERE r.endTime > current_timestamp() " +
//            "AND r.team IN (SELECT t FROM Team t WHERE t.course.clusterId = :cluster)")
//    List<Reservation> findAllActiveReservationsForCluster(@Param("cluster") UUID clusterId);
}
