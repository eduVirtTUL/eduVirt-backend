package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.statistics.*;
import java.util.UUID;

public interface ReservationStatisticsService {
    BaseCourseStatsDto getCourseStatistics(UUID courseId);

    TeamStatsDto getTeamStatistics(UUID courseId, UUID teamId);

}