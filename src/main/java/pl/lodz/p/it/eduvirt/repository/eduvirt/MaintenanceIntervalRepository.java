package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@LoggerInterceptor
public interface MaintenanceIntervalRepository extends JpaRepository<MaintenanceInterval, UUID> {

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt > current_timestamp " +
            "AND ((mi.type = 'CLUSTER' AND mi.clusterId = :clusterId) OR mi.type = 'SYSTEM') ORDER BY mi.endAt ASC")
    Page<MaintenanceInterval> findAllActiveIntervalsForGivenCluster(@Param("clusterId") UUID clusterId, Pageable pageable);

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt > current_timestamp ORDER BY mi.endAt ASC")
    Page<MaintenanceInterval> findAllActiveIntervals(Pageable pageable);

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt < current_timestamp " +
            "AND ((mi.type = 'CLUSTER' AND mi.clusterId = :clusterId) OR mi.type = 'SYSTEM') ORDER BY mi.endAt DESC")
    Page<MaintenanceInterval> findAllHistoricalIntervalsForGivenCluster(@Param("clusterId") UUID clusterId, Pageable pageable);

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt < current_timestamp ORDER BY mi.endAt DESC")
    Page<MaintenanceInterval> findAllHistoricalIntervals(Pageable pageable);

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.endAt > :start AND mi.beginAt < :end")
    List<MaintenanceInterval> findAllIntervalsInGivenTimePeriod(@Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end);

    @Query("SELECT mi FROM MaintenanceInterval mi WHERE mi.type = :type AND mi.clusterId = :clusterId AND " +
            "mi.endAt > :start AND mi.beginAt < :end")
    List<MaintenanceInterval> findAllIntervalsInGivenTimePeriod(@Param("start") LocalDateTime start,
                                                                @Param("end") LocalDateTime end,
                                                                @Param("type") MaintenanceInterval.IntervalType type,
                                                                @Param("clusterId") UUID clusterId);
}
