package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import pl.lodz.p.it.eduvirt.dto.statistics.*;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.repository.ReservationRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.ReservationStatisticsService;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRED)
public class ReservationStatisticsServiceImpl implements ReservationStatisticsService {

    private final ReservationRepository reservationRepository;
    private final CourseService courseService;
    private final ResourceGroupPoolRepository resourceGroupPoolRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public CourseStatsDto getCourseStatistics(UUID courseId) {
        Course course = courseService.getCourse(courseId);
        List<Reservation> reservations = reservationRepository.findAllByCourseId(courseId);

        Map<String, Integer> reservationsPerTeam = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTeam().getName(),
                        Collectors.summingInt(r -> 1)
                ));

        Map<String, Long> hoursPerTeam = reservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTeam().getName(),
                        Collectors.summingLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toHours())
                ));

        return new CourseStatsDto(
                course.getId(),
                course.getName(),
                reservations.size(),
                hoursPerTeam.values().stream().mapToLong(Long::longValue).sum(),
                calculateAverageLength(reservations),
                (int) reservations.stream().map(r -> r.getTeam().getId()).distinct().count(),
                reservationsPerTeam,
                hoursPerTeam
        );
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public TeamStatsDto getTeamStatistics(UUID courseId, UUID teamId) {
        List<Reservation> teamReservations = filterHistoricalReservations(
                reservationRepository.findAllByTeamId(teamId)
        );

        if (teamReservations.isEmpty()) {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new TeamNotFoundException(teamId));
            return new TeamStatsDto(
                    team.getId(),
                    team.getName(),
                    0,
                    0L,
                    0.0,
                    0,
                    0,
                    List.of()
            );
        }

        Team team = teamReservations.getFirst().getTeam();

        int statefulCount = (int) teamReservations.stream()
                .filter(r -> !resourceGroupPoolRepository.getResourceGroupPoolByResourceGroupsContaining(r.getResourceGroup()).isPresent())
                .count();

        int statelessCount = (int) teamReservations.stream()
                .filter(r -> resourceGroupPoolRepository.getResourceGroupPoolByResourceGroupsContaining(r.getResourceGroup()).isPresent())
                .count();

        List<ReservationTimelineDto> timeline = teamReservations.stream()
                .map(r -> new ReservationTimelineDto(
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getResourceGroup().getName(),
                        calculateHoursWithPrecision(r.getStartTime(), r.getEndTime())
                ))
                .toList();

        return new TeamStatsDto(
                team.getId(),
                team.getName(),
                teamReservations.size(),
                calculateTotalHours(teamReservations),
                calculateAverageLength(teamReservations),
                statefulCount,
                statelessCount,
                timeline
        );
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public ResourceStatsDto getResourceStatistics(UUID courseId, UUID resourceId, boolean isPool) {
        List<Reservation> resourceReservations;
        String name;

        if (isPool) {
            ResourceGroupPool pool = resourceGroupPoolRepository.findById(resourceId).orElse(null);
            if (pool == null) {
                return null;
            }
            resourceReservations = pool.getResourceGroups().stream()
                    .flatMap(rg -> reservationRepository.findAllByResourceGroupId(rg.getId()).stream())
                    .toList();
            name = pool.getName();
        } else {
            resourceReservations = reservationRepository.findAllByResourceGroupId(resourceId);
            name = resourceReservations.isEmpty() ? "" : resourceReservations.get(0).getResourceGroup().getName();
        }

        if (resourceReservations.isEmpty()) {
            return null;
        }

        Map<String, Integer> reservationsPerTeam = resourceReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTeam().getName(),
                        Collectors.summingInt(r -> 1)
                ));

        Map<String, Long> hoursPerTeam = resourceReservations.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTeam().getName(),
                        Collectors.summingLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toHours())
                ));

        List<ReservationTimelineDto> timeline = resourceReservations.stream()
                .map(r -> new ReservationTimelineDto(
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getTeam().getName(),
                        Duration.between(r.getStartTime(), r.getEndTime()).toHours()
                ))
                .sorted(Comparator.comparing(ReservationTimelineDto::getStartTime))
                .toList();

        return new ResourceStatsDto(
                resourceId,
                name,
                !isPool,
                resourceReservations.size(),
                calculateTotalHours(resourceReservations),
                (int) resourceReservations.stream().map(r -> r.getTeam().getId()).distinct().count(),
                reservationsPerTeam,
                hoursPerTeam,
                timeline
        );
    }

    private List<Reservation> filterHistoricalReservations(List<Reservation> reservations) {
        return reservations.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.COMPLETED
                        || r.getStatus() == Reservation.ReservationStatus.IN_PROGRESS)
                .toList();
    }

    private long calculateTotalHours(List<Reservation> reservations) {
        return reservations.stream()
                .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toHours())
                .sum();
    }

    private double calculateHoursWithPrecision(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);
        return duration.toMinutes() / 60.0;
    }

    private double calculateAverageLength(List<Reservation> reservations) {
        return reservations.stream()
                .mapToLong(r -> Duration.between(r.getStartTime(), r.getEndTime()).toHours())
                .average()
                .orElse(0.0);
    }
}