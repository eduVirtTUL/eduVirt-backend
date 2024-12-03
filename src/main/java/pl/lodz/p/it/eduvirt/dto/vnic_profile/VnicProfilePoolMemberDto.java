package pl.lodz.p.it.eduvirt.dto.vnic_profile;

import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;

import java.util.Optional;

public record VnicProfilePoolMemberDto(
        String id,
        boolean inUse
) {

//    public static VnicProfilePoolMemberDto fromEntity(VnicProfilePoolMember entity) {
//        return Optional.ofNullable(entity)
//                .map(e -> new VnicProfilePoolMemberDto(e.getId().toString(), e.getInUse()))
//                .orElse(null);
//    }
}
