package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;

@Mapper(componentModel = "spring")
public interface HostMapper {

    default HostDto ovirtHostToDto(Host host, Cluster cluster) {
        return new HostDto(
            host.id(),
            host.name(),
            host.address(),
            host.comment(),
            StatisticsUtil.getNumberOfCpus(host, cluster),
            host.maxSchedulingMemoryAsLong()
        );
    }
}
