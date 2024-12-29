package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Vm;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmGeneralDto;

import java.util.List;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface VmMapper {

    @Mapping(target = "id", expression = "java(vm.id())")
    @Mapping(target = "name", expression = "java(vm.name())")
    VmDto ovirtVmToDto(Vm vm);

    List<VmDto> ovirtVmsToDtos(Stream<Vm> vms);

    @Mapping(target = "id", expression = "java(vm.id())")
    @Mapping(target = "name", expression = "java(vm.name())")
    @Mapping(target = "status", expression = "java(vm.status().value())")
    @Mapping(target = "uptimeSeconds", source = "uptime")
    @Mapping(target = "cpuUsagePercentage", source = "cpuUsage")
    @Mapping(target = "memoryUsagePercentage", source = "memoryUsage")
    @Mapping(target = "networkUsagePercentage", source = "networkUsage")
    VmGeneralDto ovirtVmToGeneralDto(Vm vm, String uptime, String cpuUsage,
                                     String memoryUsage, String networkUsage);
}
