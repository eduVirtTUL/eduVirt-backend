package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDto;
import pl.lodz.p.it.eduvirt.dto.NetworkDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterDetailsDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterGeneralDto;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmGeneralDto;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/clusters")
@Transactional(propagation = Propagation.NEVER)
public class ClusterController {

    /* Services*/

    private final OVirtClusterService clusterService;
    private final OVirtVmService vmService;

    /* Mappers */

    private final ClusterMapper clusterMapper;
    private final HostMapper hostMapper;
    private final NetworkMapper networkMapper;
    private final VmMapper vmMapper;
    private final EventMapper eventMapper;

    /* Read methods */

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusterDetailsDto> findClusterById(@PathVariable("id") UUID clusterId) {
        Cluster foundCluster = clusterService.findClusterById(clusterId);
        return ResponseEntity.ok(clusterMapper.ovirtClusterToDetailsDto(foundCluster));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('administrator')")
    public ResponseEntity<List<ClusterGeneralDto>> findAllClusters(@PageableDefault Pageable pageable) {
        List<Cluster> clusters = clusterService.findClusters(pageable);
        List<ClusterGeneralDto> listOfDTOs = clusters.stream().map(cluster -> {
            Long hostCount = (long) clusterService.findHostCountInCluster(cluster);
            Long vmCount = (long) clusterService.findVmCountInCluster(cluster);
            return clusterMapper.ovirtClusterToGeneralDto(cluster, hostCount, vmCount);
        }).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}/hosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<HostDto>> findHostInfoByClusterId(
            @PathVariable("id") UUID clusterId, @PageableDefault Pageable pageable) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Host> hosts = clusterService.findHostsInCluster(cluster, pageable);

        List<HostDto> listOfDTOs = hosts.stream().map(host -> hostMapper.ovirtHostToDto(host, cluster)).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}/vms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VmGeneralDto>> findVirtualMachinesByClusterId(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Vm> vms = clusterService.findVmsInCluster(cluster, page, size);

        List<VmGeneralDto> listOfDTOs = vms.stream().map(vm -> {
            List<Statistic> statisticList = vmService.findStatisticsByVm(vm);

            Optional<BigDecimal> elapsedTime = StatisticsUtil.getStatisticSingleValue("elapsed.time", statisticList);
            Optional<BigDecimal> cpuUsage = StatisticsUtil.getStatisticSingleValue("cpu.usage.history", statisticList);
            Optional<BigDecimal> memoryUsage = StatisticsUtil.getStatisticSingleValue("memory.usage.history", statisticList);
            Optional<BigDecimal> networkUsage = StatisticsUtil.getStatisticSingleValue("network.usage.history", statisticList);

            String elapsedTimeValue = elapsedTime.map(BigDecimal::toPlainString).orElse(null);
            String cpuUsageValue = cpuUsage.map(BigDecimal::toPlainString).orElse(null);
            String memoryUsageValue = memoryUsage.map(BigDecimal::toPlainString).orElse(null);
            String networkUsageValue = networkUsage.map(BigDecimal::toPlainString).orElse(null);

            return vmMapper.ovirtVmToGeneralDto(vm, elapsedTimeValue, cpuUsageValue, memoryUsageValue, networkUsageValue);
        }).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}/networks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<NetworkDto>> findNetworksByClusterId(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Network> networks = clusterService.findNetworksInCluster(cluster, page, size);

        List<NetworkDto> listOfDTOs = networks.stream().map(networkMapper::ovirtNetworkToDto).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventGeneralDto>> findEventsByClusterId(
            @PathVariable("id") UUID clusterId, @PageableDefault Pageable pageable) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Event> events = clusterService.findEventsInCluster(cluster, pageable);

        List<EventGeneralDto> listOfDTOs = events.stream()
                .map(eventMapper::ovirtEventToGeneralDTO).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }
}
