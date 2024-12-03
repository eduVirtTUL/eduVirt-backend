package pl.lodz.p.it.eduvirt.dto.vm;

public record VmGeneralDto(
        String id,
        String name,
        String status,
        String uptimeSeconds,
        String cpuUsagePercentage,
        String memoryUsagePercentage,
        String networkUsagePercentage
) {
}
