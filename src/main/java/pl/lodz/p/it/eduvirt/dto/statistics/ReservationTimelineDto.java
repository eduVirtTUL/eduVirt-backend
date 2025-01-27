package pl.lodz.p.it.eduvirt.dto.statistics;

import lombok.Value;
import java.time.LocalDateTime;

@Value
public class ReservationTimelineDto {
    LocalDateTime startTime;
    LocalDateTime endTime;
    String resourceName;
    double lengthInHours;
}