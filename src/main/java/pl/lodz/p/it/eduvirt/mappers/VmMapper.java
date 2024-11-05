package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Vm;
import pl.lodz.p.it.eduvirt.dto.VmDto;
import pl.lodz.p.it.eduvirt.dto.VmGeneralDto;

@Mapper(componentModel = "spring")
public interface VmMapper {

    @Mapping(target = "id", expression = "java(vm.id())")
    @Mapping(target = "name", expression = "java(vm.name())")
    VmDto ovirtVmToDto(Vm vm);

    default VmGeneralDto ovirtVmToGeneralDto(Vm vm, String uptime, String cpuUsage,
                                             String memoryUsage, String networkUsage) {
        return new VmGeneralDto(
                vm.id(),
                vm.name(),
                vm.status().value(),
                uptime,
                cpuUsage,
                memoryUsage,
                networkUsage
        );
    }
}
