package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;

@Mapper(componentModel = "spring", imports = {StatisticsUtil.class})
public interface HostMapper {

    @Mapping(target = "id", expression = "java(host.id())")
    @Mapping(target = "name", expression = "java(host.name())")
    @Mapping(target = "address", expression = "java(host.address())")
    @Mapping(target = "comment", expression = "java(host.comment())")
    @Mapping(target = "cpus", expression = "java(StatisticsUtil.getNumberOfCpus(host, cluster))")
    @Mapping(target = "memory", expression = "java(host.maxSchedulingMemoryAsLong())")
    HostDto ovirtHostToDto(Host host, Cluster cluster);
}
