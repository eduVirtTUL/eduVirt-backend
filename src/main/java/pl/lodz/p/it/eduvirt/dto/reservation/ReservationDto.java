package pl.lodz.p.it.eduvirt.dto.reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationDto(
    UUID id,
    UUID teamId,
    LocalDateTime start,
    LocalDateTime end
) {}
