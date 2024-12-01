package pl.lodz.p.it.eduvirt.dto.cluster;

public record ClusterGeneralDto(
        String id,
        String name,
        String description,
        String comment,
        String clusterCpuType,
        String compatibilityVersion,
        Long hostCount,
        Long vmCount
) {}
