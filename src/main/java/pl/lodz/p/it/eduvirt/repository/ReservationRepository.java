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
import pl.lodz.p.it.eduvirt.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@LoggerInterceptor
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

    @PreAuthorize("hasRole('administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.team.course.clusterId = :clusterId " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findClusterReservations(@Param("clusterId") UUID clusterId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    @PreAuthorize("hasRole('administrator')")
    @Query("SELECT r FROM Reservation r WHERE NOT((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findSystemReservations(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgReservationsForGivenTeam(
            @Param("rg") ResourceGroup rg, @Param("team") Team team,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp) " +
            "AND NOT ((r.startTime <= :start AND r.endTime <= :start) " +
            "OR (r.startTime >= :end AND r.endTime >= :end))")
    List<Reservation> findRgPoolReservationsForGivenTeam(
            @Param("rgp") ResourceGroupPool rgp, @Param("team") Team team,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team")
    List<Reservation> findAllRgReservationsForGivenTeam(@Param("rg") ResourceGroup rg,
                                                        @Param("team") Team team);

    @Query("SELECT r FROM Reservation r WHERE r.resourceGroup = :rg AND r.team = :team")
    Page<Reservation> findAllRgReservationsForGivenTeam(@Param("rg") ResourceGroup rg,
                                                        @Param("team") Team team, Pageable pageable);

    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp)")
    List<Reservation> findAllRgPoolReservationsForGivenTeam(@Param("rgp") ResourceGroupPool rgp,
                                                            @Param("team") Team team);

    @Query("SELECT r FROM Reservation r WHERE r.team = :team AND " +
            "r.resourceGroup IN (SELECT rgp.resourceGroups FROM ResourceGroupPool rgp WHERE rgp = :rgp)")
    Page<Reservation> findAllRgPoolReservationsForGivenTeam(@Param("rgp") ResourceGroupPool rgp,
                                                            @Param("team") Team team, Pageable pageable);

    /* Other */

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime > :probeTime AND r.team = :team")
    Page<Reservation> findAllActiveReservations(@Param("team") Team team,
                                                @Param("probeTime") LocalDateTime probeTime,
                                                Pageable pageable);

    @PreAuthorize("hasAnyRole('teacher', 'administrator')")
    @Query("SELECT r FROM Reservation r WHERE r.endTime <= :probeTime AND r.team = :team")
    Page<Reservation> findAllHistoricalReservations(@Param("team") Team team,
                                                    @Param("probeTime") LocalDateTime probeTime,
                                                    Pageable pageable);
}
