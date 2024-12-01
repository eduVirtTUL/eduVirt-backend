package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.ovirt.engine.sdk4.types.Cluster;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterDetailsDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterGeneralDto;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    default ClusterGeneralDto ovirtClusterToGeneralDto(Cluster cluster, Long hostCount, Long vmCount) {
        return new ClusterGeneralDto(
                cluster.id(),
                cluster.name(),
                cluster.description(),
                cluster.comment(),
                cluster.cpu().type(),
                "%s.%s".formatted(cluster.version().major(), cluster.version().minor()),
                hostCount,
                vmCount
        );
    }

    default ClusterDetailsDto ovirtClusterToDetailsDto(Cluster cluster) {
        return new ClusterDetailsDto(
                cluster.id(),
                cluster.name(),
                cluster.description(),
                cluster.comment(),
                cluster.cpu().type(),
                "%s.%s".formatted(cluster.version().major(), cluster.version().minor()),
                cluster.threadsAsCores(),
                cluster.memoryPolicy().overCommit().percent() + "%"
        );
    }
}
