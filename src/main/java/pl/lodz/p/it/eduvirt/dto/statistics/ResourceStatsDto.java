package pl.lodz.p.it.eduvirt.dto.statistics;

import lombok.Value;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Value
public class ResourceStatsDto {
    UUID resourceId;
    String resourceName;
    boolean isStateful;
    int totalReservations;
    long totalHours;
    int uniqueTeamsCount;
    Map<String, Integer> reservationsPerTeam;
    Map<String, Long> hoursPerTeam;
    List<ReservationTimelineDto> timeline;
}