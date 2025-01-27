package pl.lodz.p.it.eduvirt.dto.statistics;

import java.util.Map;
import java.util.UUID;

import lombok.Value;

@Value
public class CourseStatsDto implements BaseCourseStatsDto {
    UUID courseId;
    String courseName;
    int totalReservations;
    double totalHours;
    double averageLength;
    int totalTeams;
    Map<String, Integer> reservationsPerTeam;
    Map<String, Double> hoursPerTeam;
}