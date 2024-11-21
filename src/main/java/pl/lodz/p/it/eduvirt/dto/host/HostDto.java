package pl.lodz.p.it.eduvirt.dto;

public record HostDto(
        String id,
        String name,
        String address,
        String comment,

        Long cpus,
        Long memory
) {}
