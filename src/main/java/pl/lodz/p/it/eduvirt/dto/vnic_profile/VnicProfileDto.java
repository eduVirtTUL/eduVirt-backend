package pl.lodz.p.it.eduvirt.dto.vnic_profile;

import java.util.List;

public record VnicProfileDto(
        String id,
        String name,
        String networkId,
        String networkName,
        String networkVlanId,
        Boolean inPool,
        Boolean valid,
        List<String> validationErrors
) {}
