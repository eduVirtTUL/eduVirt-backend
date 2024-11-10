package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.*;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.util.StatisticsUtil;

import java.math.BigDecimal;
import java.util.*;

@RestController
@LoggerInterceptor
@RequestMapping(path = "/clusters")
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class ClusterController {

    private final ClusterMapper clusterMapper;
    private final HostMapper hostMapper;
    private final NetworkMapper networkMapper;
    private final EventMapper eventMapper;
    private final VmMapper vmMapper;

    private final OVirtClusterService clusterService;
    private final OVirtVmService vmService;

    // Read methods

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findClusterById(@PathVariable("id") UUID clusterId) {
        Cluster foundCluster = clusterService.findClusterById(clusterId);
        return ResponseEntity.ok(clusterMapper.ovirtClusterToDetailsDto(foundCluster));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAllClusters(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        List<Cluster> clusters = clusterService.findClusters(pageNumber, pageSize);
        List<ClusterGeneralDto> listOfDTOs = clusters.stream().map(cluster -> {
            Long hostCount = (long) clusterService.findHostCountInCluster(cluster);
            Long vmCount = (long) clusterService.findVmCountInCluster(cluster);
            return clusterMapper.ovirtClusterToGeneralDto(cluster, hostCount, vmCount);
        }).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping(path = "/{id}/hosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findCpuInfoByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                    @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Host> hosts = clusterService.findHostsInCluster(cluster, pageNumber, pageSize);

        List<HostDto> listOfDTOs = hosts.stream().map(host -> hostMapper.ovirtHostToDto(host, cluster)).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping(path = "/{id}/vms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findVirtualMachinesByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                            @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Vm> vms = clusterService.findVmsInCluster(cluster, pageNumber, pageSize);

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

    @GetMapping(path = "/{id}/networks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findNetworksByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                     @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                     @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Network> networks = clusterService.findNetworksInCluster(cluster, pageNumber, pageSize);

        List<NetworkDto> listOfDTOs = networks.stream().map(networkMapper::ovirtNetworkToDto).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping(path = "/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findEventsByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                   @RequestParam(value = "pageSize", defaultValue = "25", required = false) int pageSize,
                                                   @PathVariable("id") UUID clusterId) {
        Cluster cluster = clusterService.findClusterById(clusterId);
        List<Event> events = clusterService.findEventsInCluster(cluster, pageNumber, pageSize);

        List<EventGeneralDTO> listOfDTOs = events.stream().map(eventMapper::ovirtEventToGeneralDTO).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }
}
