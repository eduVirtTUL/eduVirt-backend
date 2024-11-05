package pl.lodz.p.it.eduvirt.dto;

public record DataCenterDto(
        String id,
        String name,
        String description,
        String comment,
        String status,
        String compatibilityVersion
) {}
