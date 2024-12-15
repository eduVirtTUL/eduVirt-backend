package pl.lodz.p.it.eduvirt.dto.reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateReservationDto(
        UUID resourceGroupId,
        LocalDateTime start,
        LocalDateTime end,
        boolean automaticStartup
) {}
