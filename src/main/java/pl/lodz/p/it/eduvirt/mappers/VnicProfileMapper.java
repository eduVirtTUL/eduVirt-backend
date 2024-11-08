package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.VnicProfile;
import pl.lodz.p.it.eduvirt.dto.VnicProfileDto;

@Mapper(componentModel = "spring")
public interface VnicProfileMapper {

    @Mapping(target = "id",             expression = "java(vnicProfile.id())")
    @Mapping(target = "name",           expression = "java(vnicProfile.name())")
    @Mapping(target = "networkId",      expression = "java(vnicProfile.network().id())")
    @Mapping(target = "networkName",    expression = "java(vnicProfile.network().name())")
    @Mapping(target = "networkVlanId",  expression = "java(vnicProfile.network().vlan() != null ? vnicProfile.network().vlan().id().toString() : null)")
    VnicProfileDto ovirtVnicProfileToDto(VnicProfile vnicProfile);
}