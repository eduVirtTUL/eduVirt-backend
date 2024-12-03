package pl.lodz.p.it.eduvirt.dto.vnic_profile;

public record OvirtVnicProfileDto(
        String id,
        String name,
        String networkId,
        String networkName,
        String networkVlanId
) {}
