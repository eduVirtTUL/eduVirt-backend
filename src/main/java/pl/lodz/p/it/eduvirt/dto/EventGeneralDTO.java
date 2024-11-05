package pl.lodz.p.it.eduvirt.dto;

public record EventGeneralDTO(
        String id,
        String message,
        String severity,
        String registeredAt
) {}
