package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.*;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.*;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;
import pl.lodz.p.it.eduvirt.util.connection.PaginationUtil;
import pl.lodz.p.it.eduvirt.util.connection.StatisticsUtil;

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

    private final ConnectionFactory connectionFactory;
    private final VmMapper vmMapper;

    // Read methods

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findClusterById(@PathVariable("id") UUID clusterId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());

            Cluster cluster = clusterService.get().send().cluster();

            return ResponseEntity.ok(clusterMapper.ovirtClusterToDetailsDto(cluster));
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findAllClusters(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                             @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize) {
        SystemService systemService = connectionFactory.getConnection().systemService();
        ClustersService clustersService = systemService.clustersService();

        String searchQuery = "page %s".formatted(pageNumber + 1);
        List<ClusterGeneralDto> listOfDTOs =  clustersService.list().search(searchQuery)
                .max(pageSize).send().clusters().stream().map(cluster -> {
            Long hostCount = (long) systemService.hostsService().list()
                    .search("cluster=" + cluster.name())
                    .send().hosts().size();

            Long vmCount = (long) systemService.vmsService().list()
                    .search("cluster=" + cluster.name())
                    .send().vms().size();

            return clusterMapper.ovirtClusterToGeneralDto(cluster, hostCount, vmCount);
        }).toList();

        if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @GetMapping(path = "/{id}/hosts", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findCpuInfoByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                    @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                    @PathVariable("id") UUID clusterId) {
        try {
            Connection connection =  connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());

            Cluster cluster = clusterService.get().send().cluster();
            HostsService hostsService = systemService.hostsService();

            String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
            List<Host> foundHosts = hostsService.list().search(searchQuery).max(pageSize).send().hosts();
            List<HostDto> listOfDTOs = foundHosts.stream().map(host -> hostMapper.ovirtHostToDto(host, cluster)).toList();

            if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDTOs);
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }

    @GetMapping(path = "/{id}/vms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findVirtualMachinesByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                            @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                            @PathVariable("id") UUID clusterId) {
        try {
            Connection connection =  connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());

            Cluster cluster = clusterService.get().send().cluster();
            VmsService vmsService = systemService.vmsService();

            String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
            List<Vm> foundVms = vmsService.list().search(searchQuery).max(pageSize).send().vms();

            List<VmGeneralDto> listOfDTOs = foundVms.stream().map(vm -> {
                List<Statistic> statisticList = connection.followLink(vm.statistics());

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
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }

    @GetMapping(path = "/{id}/networks", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findNetworksByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                     @RequestParam(value = "pageSize", defaultValue = "10", required = false) int pageSize,
                                                     @PathVariable("id") UUID clusterId) {
        try {
            Connection connection =  connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());
            Cluster cluster = clusterService.get().send().cluster();

            Collection<Network> networks = PaginationUtil.getPaginatedCollection(
                    pageNumber, pageSize, connection.followLink(cluster.networks()));
            List<NetworkDto> listOfDTOs = networks.stream().map(networkMapper::ovirtNetworkToDto).toList();

            if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDTOs);
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }

    @GetMapping(path = "/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> findEventsByClusterId(@RequestParam(value = "pageNumber", defaultValue = "0", required = false) int pageNumber,
                                                   @RequestParam(value = "pageSize", defaultValue = "25", required = false) int pageSize,
                                                   @PathVariable("id") UUID clusterId) {
        try {
            Connection connection =  connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());
            Cluster cluster = clusterService.get().send().cluster();

            String args = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
            List<Event> events = systemService.eventsService().list().search(args).max(pageSize).send().events();
            List<EventGeneralDTO> listOfDTOs = events.stream().map(eventMapper::ovirtEventToGeneralDTO).toList();

            if (listOfDTOs.isEmpty()) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(listOfDTOs);
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }
}
