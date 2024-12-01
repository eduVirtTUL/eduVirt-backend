package pl.lodz.p.it.eduvirt.dto.host;

public record HostDto(
        String id,
        String name,
        String address,
        String comment,

        Long cpus,
        Long memory
) {}
