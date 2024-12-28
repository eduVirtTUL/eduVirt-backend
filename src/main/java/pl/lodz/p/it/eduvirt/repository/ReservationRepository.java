package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(propagation = Propagation.MANDATORY)
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end AND r.resourceGroup = :rg")
    List<Reservation> findReservationForGivenPeriodForResourceGroup(
            @Param("rg") ResourceGroup resourceGroup, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @PreAuthorize("hasRole('administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end AND r.team.course.clusterId = :clusterId")
    List<Reservation> findReservationsForGivenPeriodForCluster(
            @Param("clusterId") UUID clusterId, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @PreAuthorize("hasRole('administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :start AND r.startTime < :end")
    List<Reservation> findReservationsForGivenPeriodForSystem(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE NOT ((r.startTime <= :periodStart AND r.endTime <= :periodStart) " +
            "OR (r.startTime >= :periodEnd AND r.endTime >= :periodEnd)) " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course = :course)")
    List<Reservation> findCurrentReservationsForCourse(@Param("course") Course course,
                                                       @Param("periodStart") LocalDateTime periodStart,
                                                       @Param("periodEnd") LocalDateTime periodEnd);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE NOT ((r.startTime <= :periodStart AND r.endTime <= :periodStart) " +
            "OR (r.startTime >= :periodEnd AND r.endTime >= :periodEnd)) " +
            "AND r.team IN (SELECT t FROM Team t WHERE t.course.clusterId = :cluster)")
    List<Reservation> findCurrentReservationsForCluster(@Param("cluster") UUID clusterId,
                                                        @Param("periodStart") LocalDateTime periodStart,
                                                        @Param("periodEnd") LocalDateTime periodEnd);

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :probeTime AND r.team = :team")
    Page<Reservation> findAllActiveReservations(@Param("team") Team team, @Param("probeTime") LocalDateTime probeTime, Pageable pageable);

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime <= :probeTime AND r.team = :team")
    Page<Reservation> findAllHistoricalReservations(@Param("team") Team team, @Param("probeTime") LocalDateTime probeTime, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :resourceGroup AND r.team = :team " +
            "AND (r.startTime < :to AND r.endTime > :from)")
    List<Reservation> findResourceGroupReservationForGivenTeamInTimePeriod(
            @Param("resourceGroup") ResourceGroup resourceGroup, @Param("team") Team team,
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

//    // TODO: Uncomment when clusterId becomes a part of Course
//    @Query("SELECT r FROM Reservation r WHERE r.endTime > current_timestamp() " +
//            "AND r.team IN (SELECT t FROM Team t WHERE t.course.clusterId = :cluster)")
//    List<Reservation> findAllActiveReservationsForCluster(@Param("cluster") UUID clusterId);

    //TODO michal: maybe add flag to reservation or check executor_task table to check which reservation was processing
    //TODO michal: optimization
    //TODO michal: change r.endTime to 'r.endTime - 5 minutes' for ex. -> starting reservations for a few seconds makes no sense..
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE current_timestamp BETWEEN r.startTime AND r.endTime
            AND r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_INIT' AND et.status != 'FAILED')
            """)
    List<Reservation> findReservationsToBegin();

    //TODO michal: r.endTime - 5 minutes -> due to the potential start of the next reservation immediately after this one
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE current_timestamp >= r.endTime
            AND r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_DESTRUCT' AND et.status != 'FAILED')
            """)
    List<Reservation> findReservationsToFinish();
}
