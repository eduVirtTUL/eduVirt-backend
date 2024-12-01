package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.CpuTopology;
import org.ovirt.engine.sdk4.types.Host;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;

import java.math.BigInteger;

@Mapper(componentModel = "spring")
public interface HostMapper {

    default HostDto ovirtHostToDto(Host host, Cluster cluster) {
        return new HostDto(
            host.id(),
            host.name(),
            host.address(),
            host.comment(),
            getNumberOfCpus(host, cluster),
            host.maxSchedulingMemoryAsLong()
        );
    }

    static Long getNumberOfCpus(Host host, Cluster cluster) {
        CpuTopology topology = host.cpu().topology();
        BigInteger cpuCount = topology.sockets().multiply(topology.cores());
        return cluster.threadsAsCores() ? cpuCount.multiply(topology.threads()).longValue() : cpuCount.longValue();
    }
}
