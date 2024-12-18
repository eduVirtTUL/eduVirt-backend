package pl.lodz.p.it.eduvirt.dto.resources;

import java.time.LocalDateTime;

public record ResourcesAvailabilityDto(
        LocalDateTime time,
        boolean available
) {}
