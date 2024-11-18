package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.VnicProfile;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfileDto;
import pl.lodz.p.it.eduvirt.dto.vnic_profile.VnicProfilePoolMemberDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.network.VnicProfilePoolMember;

@Mapper(componentModel = "spring")
public interface VnicProfileMapper {

    @Mapping(target = "id",             expression = "java(vnicProfile.id())")
    @Mapping(target = "name",           expression = "java(vnicProfile.name())")
    @Mapping(target = "networkId",      expression = "java(vnicProfile.network().id())")
    @Mapping(target = "networkName",    expression = "java(vnicProfile.network().name())")
    @Mapping(target = "networkVlanId",  expression = "java(vnicProfile.network().vlan() != null ? vnicProfile.network().vlan().id().toString() : null)")
    @Mapping(target = "isInPool",       expression = "java(isInPool)")
    VnicProfileDto ovirtVnicProfileToDto(VnicProfile vnicProfile, Boolean isInPool);

    VnicProfilePoolMemberDto vnicProfileToDto(VnicProfilePoolMember vnicProfile);
}