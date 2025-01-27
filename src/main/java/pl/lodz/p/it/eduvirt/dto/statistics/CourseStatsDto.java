package pl.lodz.p.it.eduvirt.dto.statistics;

import java.util.Map;
import java.util.UUID;

import lombok.Value;

@Value
public class CourseStatsDto {
    UUID courseId;
    String courseName;
    int totalReservations;
    long totalReservationHours;
    double averageReservationLength;
    int activeTeamsCount;
    Map<String, Integer> reservationsPerTeam;
    Map<String, Long> hoursPerTeam;
}