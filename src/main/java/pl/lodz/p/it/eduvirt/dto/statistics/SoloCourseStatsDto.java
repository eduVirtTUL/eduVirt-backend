package pl.lodz.p.it.eduvirt.dto.statistics;

import lombok.Value;
import java.util.UUID;

@Value
public class SoloCourseStatsDto implements BaseCourseStatsDto {
    UUID courseId;
    String courseName;
    int totalReservations;
    double totalHours;
    double averageLength;
    int totalTeams;
}