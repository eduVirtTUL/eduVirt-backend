package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.dto.statistics.*;
import java.util.UUID;

public interface ReservationStatisticsService {
    CourseStatsDto getCourseStatistics(UUID courseId);

    TeamStatsDto getTeamStatistics(UUID courseId, UUID teamId);

    ResourceStatsDto getResourceStatistics(UUID courseId, UUID resourceId, boolean isPool);
}