package pl.lodz.p.it.eduvirt.dto;

public record VnicProfileDto(
        String id,
        String name,
        String networkId,
        String networkName,
        String networkVlanId
) {}
