package pl.lodz.p.it.eduvirt.dto.vnic_profile;

public record VnicProfileDto(
        String id,
        String name,
        String networkId,
        String networkName,
        String networkVlanId,
        Boolean inPool
) {}
