package pl.lodz.p.it.eduvirt.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.ovirt.engine.sdk4.types.Cluster;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterDetailsDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterGeneralDto;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    @Mapping(target = "id", expression = "java(cluster.id())")
    @Mapping(target = "name", expression = "java(cluster.name())")
    @Mapping(target = "description", expression = "java(cluster.description())")
    @Mapping(target = "comment", expression = "java(cluster.comment())")
    @Mapping(target = "clusterCpuType", expression = "java(cluster.cpu().type())")
    @Mapping(target = "compatibilityVersion", expression = "java(\"%s.%s\".formatted(cluster.version().major(), cluster.version().minor()))")
    @Mapping(target = "hostCount", source = "hostCount")
    @Mapping(target = "vmCount", source = "vmCount")
    ClusterGeneralDto ovirtClusterToGeneralDto(Cluster cluster, Long hostCount, Long vmCount);

    @Mapping(target = "id", expression = "java(cluster.id())")
    @Mapping(target = "name", expression = "java(cluster.name())")
    @Mapping(target = "description", expression = "java(cluster.description())")
    @Mapping(target = "comment", expression = "java(cluster.comment())")
    @Mapping(target = "clusterCpuType", expression = "java(cluster.cpu().type())")
    @Mapping(target = "compatibilityVersion", expression = "java(\"%s.%s\".formatted(cluster.version().major(), cluster.version().minor()))")
    @Mapping(target = "threadsAsCores", expression = "java(cluster.threadsAsCores())")
    @Mapping(target = "maxMemoryOverCommit", expression = "java(cluster.memoryPolicy().overCommit().percent() + \"%\")")
    ClusterDetailsDto ovirtClusterToDetailsDto(Cluster cluster);
}
