package pl.lodz.p.it.eduvirt.dto;

import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePool;

import java.util.Optional;

public record VnicProfilePoolDto(
        String id,
        boolean in_use
) {

    public static VnicProfilePoolDto fromEntity(VnicProfilePool entity) {
        return Optional.ofNullable(entity)
                .map(e -> new VnicProfilePoolDto(e.getId().toString(), e.getInUse()))
                .orElse(null);
    }
}
