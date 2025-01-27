package pl.lodz.p.it.eduvirt.dto.statistics;

import java.util.UUID;

public interface BaseCourseStatsDto {
    UUID getCourseId();
    String getCourseName();
    int getTotalReservations();
    double getTotalHours();
    double getAverageLength();
    int getTotalTeams();
}