package pl.lodz.p.it.eduvirt.dto;

public record EventGeneralDto(
        String id,
        String message,
        String severity,
        String registeredAt
) {}
