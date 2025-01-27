package pl.lodz.p.it.eduvirt.dto.statistics;

import lombok.Value;
import java.util.List;
import java.util.UUID;

@Value
public class TeamStatsDto {
    UUID teamId;
    String teamName;
    int totalReservations;
    long totalHours;
    double averageLength;
    int statefulResourceCount;
    int statelessPoolCount;
    List<ReservationTimelineDto> timeline;
}