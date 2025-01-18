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
import pl.lodz.p.it.eduvirt.entity.MaintenanceInterval;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@LoggerInterceptor
@Transactional(propagation = Propagation.MANDATORY)
public interface MaintenanceIntervalRepository extends JpaRepository<MaintenanceInterval, UUID> {

    @PreAuthorize("hasAuthority('administrator')")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt >= :probeTime " +
            "AND ((mi.type = 'CLUSTER' AND mi.clusterId = :clusterId) OR mi.type = 'SYSTEM')")
    Page<MaintenanceInterval> findAllActiveIntervalsForGivenCluster(@Param("clusterId") UUID clusterId,
                                                                    @Param("probeTime") LocalDateTime probeTime,
                                                                    Pageable pageable);

    @PreAuthorize("hasAuthority('administrator')")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt >= :probeTime")
    Page<MaintenanceInterval> findAllActiveIntervals(@Param("probeTime") LocalDateTime probeTime,
                                                     Pageable pageable);

    @PreAuthorize("hasAuthority('administrator')")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt < :probeTime " +
            "AND ((mi.type = 'CLUSTER' AND mi.clusterId = :clusterId) OR mi.type = 'SYSTEM')")
    Page<MaintenanceInterval> findAllHistoricalIntervalsForGivenCluster(@Param("clusterId") UUID clusterId,
                                                                        @Param("probeTime") LocalDateTime probeTime,
                                                                        Pageable pageable);

    @PreAuthorize("hasAuthority('administrator')")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt < :probeTime")
    Page<MaintenanceInterval> findAllHistoricalIntervals(@Param("probeTime") LocalDateTime probeTime,
                                                         Pageable pageable);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE NOT (mi.endAt <= :start OR mi.beginAt >= :end) " +
            "AND (mi.clusterId IS NULL OR mi.clusterId = :cluster)")
    List<MaintenanceInterval> findAllIntervalsInGivenTimePeriod(
            @Param("cluster") UUID clusterId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @PreAuthorize("isAuthenticated()")
    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.type = :type AND mi.clusterId = :clusterId AND " +
            "NOT (mi.endAt <= :start OR mi.beginAt >= :end)")
    List<MaintenanceInterval> findAllIntervalsInGivenTimePeriod(@Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end,
                                                                @Param("type") MaintenanceInterval.IntervalType type,
                                                                @Param("clusterId") UUID clusterId);
}
