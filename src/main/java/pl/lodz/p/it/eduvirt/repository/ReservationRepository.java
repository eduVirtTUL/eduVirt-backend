package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@LoggerInterceptor
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end AND r.resourceGroup = :rg")
    List<Reservation> findReservationForGivenPeriodForResourceGroup(
            @Param("rg") ResourceGroup resourceGroup, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end AND r.team.course.clusterId = :clusterId")
    List<Reservation> findReservationsForGivenPeriodForCluster(
            @Param("clusterId") UUID clusterId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end")
    List<Reservation> findReservationsForGivenPeriodForSystem(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :probeTime " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course = :course)")
    List<Reservation> findCurrentReservationsForCourse(
            @Param("course") Course course, @Param("probeTime") LocalDateTime probeTime);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > :probeTime " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course.clusterId = :cluster)")
    List<Reservation> findCurrentReservationsForCluster(
            @Param("cluster") UUID clusterId, @Param("probeTime") LocalDateTime probeTime);

    @Query("SELECT r FROM Reservation r WHERE r.endTime > current_timestamp() AND r.team = :team")
    Page<Reservation> findAllActiveReservations(@Param("team") Team team, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.endTime <= current_timestamp() AND r.team = :team")
    Page<Reservation> findAllHistoricalReservations(@Param("team") Team team, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :resourceGroup AND r.team = :team " +
            "AND (r.startTime < :to AND r.endTime > :from)")
    List<Reservation> findResourceGroupReservationForGivenTeamInTimePeriod(
            @Param("resourceGroup") ResourceGroup resourceGroup, @Param("team") Team team,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
