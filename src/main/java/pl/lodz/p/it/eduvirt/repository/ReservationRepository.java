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
import pl.lodz.p.it.eduvirt.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    /* Check methods for Reservation creation */

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgReservations(@Param("rg") ResourceGroup resourceGroup,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup " +
            "IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp) " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgPoolReservations(@Param("rgp") ResourceGroupPool rgp,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.team.course = :course " +
            "AND NOT((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findCourseReservations(@Param("course") Course course,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.team.course.clusterId = :clusterId " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findClusterReservations(@Param("clusterId") UUID clusterId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE NOT((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findSystemReservations(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgReservationsForGivenTeam(
            @Param("rg") ResourceGroup rg, @Param("team") Team team,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp) " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgPoolReservationsForGivenTeam(
            @Param("rgp") ResourceGroupPool rgp, @Param("team") Team team,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team")
    List<Reservation> findAllRgReservationsForGivenTeam(@Param("rg") ResourceGroup rg,
                                                        @Param("team") Team team);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team")
    Page<Reservation> findAllRgReservationsForGivenTeam(@Param("rg") ResourceGroup rg,
                                                        @Param("team") Team team, Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp)")
    List<Reservation> findAllRgPoolReservationsForGivenTeam(@Param("rgp") ResourceGroupPool rgp,
                                                            @Param("team") Team team);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp)")
    Page<Reservation> findAllRgPoolReservationsForGivenTeam(@Param("rgp") ResourceGroupPool rgp,
                                                            @Param("team") Team team, Pageable pageable);

    /* Other */

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :probeTime AND r.team = :team")
    Page<Reservation> findAllActiveReservations(@Param("team") Team team,
                                                @Param("probeTime") LocalDateTime probeTime,
                                                Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT r FROM Reservation r WHERE r.endTime <= :probeTime AND r.team = :team")
    Page<Reservation> findAllHistoricalReservations(@Param("team") Team team,
                                                    @Param("probeTime") LocalDateTime probeTime,
                                                    Pageable pageable);

    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            LEFT JOIN FETCH r.resourceGroup rg
            LEFT JOIN FETCH r.team t
            WHERE t.course = :course
            AND r.status = 'IN_PROGRESS'
            """)
    Page<Reservation> findAllOngoingCourseReservations(@Param("course") Course course,
                                                       Pageable pageable);

    //probeTimeWithTimeNeededToStop == (probeTime + taskTolerance + vmGraceTime)
    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE :probeTime >= r.startTime AND :probeTimeWithTimeNeededToStop <= r.endTime
            AND (
                r.status = 'PENDING'
                OR (
                    r.status = 'IN_PROGRESS'
                    AND
                    r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_INIT' AND et.status = 'SUCCESSFUL')
                    )
                )
            """)
    List<Reservation> findAllReservationsToBegin(@Param("probeTime") LocalDateTime probeTime,
                                                 @Param("probeTimeWithTimeNeededToStop") LocalDateTime probeTimeWithTimeNeededToStop);

    @Query("""
            SELECT DISTINCT r FROM Reservation r
            WHERE :probeTimeWithTimeNeededToStop >= r.endTime
            AND r.status = 'IN_PROGRESS'
            AND r.id NOT IN (SELECT et.reservation.id FROM ExecutorTask et WHERE et.type = 'POD_DESTRUCT' AND et.status != 'FAILED')
            """)
    List<Reservation> findAllReservationsToStop(@Param("probeTimeWithTimeNeededToStop") LocalDateTime probeTimeWithTimeNeededToStop);

    @Query("""
            SELECT DISTINCT r FROM Reservation r
            JOIN FETCH r.team team
            JOIN FETCH team.users
            WHERE :probeTime < r.endTime
            AND r.status = 'IN_PROGRESS'
            AND r.id NOT IN (SELECT mn.reservation.id FROM MailNotification mn WHERE mn.type = 'RESERVATION_END')
            """)
    List<Reservation> findAllReservationsToSendNotifications(@Param("probeTime") LocalDateTime probeTime);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.status = :status")
    List<Reservation> findRgReservationsByStatus(@Param("rg") ResourceGroup resourceGroup,
                                                 @Param("status") Reservation.ReservationStatus status);

    /* Reservation statistics */

    @Query("SELECT r FROM Reservation r WHERE r.team.course.id = :courseId")
    List<Reservation> findAllByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT r FROM Reservation r WHERE r.team.id = :teamId")
    List<Reservation> findAllByTeamId(@Param("teamId") UUID teamId);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup.id = :resourceId")
    List<Reservation> findAllByResourceGroupId(@Param("resourceId") UUID resourceId);

}
