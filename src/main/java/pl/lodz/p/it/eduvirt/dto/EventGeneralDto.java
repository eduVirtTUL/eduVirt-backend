package pl.lodz.p.it.eduvirt.dto;

import java.time.LocalDateTime;

public record EventGeneralDto(
        String id,
        String message,
        String severity,
        LocalDateTime registeredAt
) {}
