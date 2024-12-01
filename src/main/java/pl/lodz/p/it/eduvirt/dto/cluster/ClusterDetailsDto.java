package pl.lodz.p.it.eduvirt.dto.cluster;

public record ClusterDetailsDto(
        String id,
        String name,
        String description,
        String comment,
        String clusterCpuType,
        String compatibilityVersion,
        Boolean useThreadsAsCpus,
        String maxMemoryOverCommit
) {}
