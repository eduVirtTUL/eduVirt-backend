package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
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

    @Operation(
        method = "GET", summary = "Get detailed information about certain cluster",
        description = "This endpoint can be used by the administrator to fetch detailed information about certain cluster, identified with the given identifier.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the cluster, which detailed information is to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Cluster's detailed information, identified with given identifier was found and sent to the client successfully."),
            @ApiResponse(responseCode = "404",
                description = "Cluster, identified with given identifier could not be found in the oVirt system.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClusterDetailsDto> findClusterById(@PathVariable("id") UUID clusterId) {
        Cluster foundCluster = clusterService.findClusterById(clusterId);
        return ResponseEntity.ok(clusterMapper.ovirtClusterToDetailsDto(foundCluster));
    }

    @Operation(
        method = "GET", summary = "Get all available clusters in the oVirt engine instance",
        description = "This endpoint can be used by the administrator to fetch all the clusters from the oVirt engine instance.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of clusters from given page of given size were found in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No cluster was found in the oVirt system (which could be due to pagination), and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

    @Operation(
        method = "GET", summary = "Get all nodes of certain cluster in the oVirt engine instance",
        description = "This endpoint can be used by the administrator to fetch all the nodes of certain cluster (hosts) from the oVirt engine instance.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the cluster, which nodes are to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "List of hosts from given page of given size were found for given cluster in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No host was found in the oVirt system (which could be due to pagination) for given cluster, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Cluster with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

    @Operation(
        method = "GET", summary = "Get detailed info on all virtual machines that are located in certain cluster in the oVirt engine instance",
        description = "This endpoint can be used by the administrator to fetch all the virtual machines assigned to certain cluster in the oVirt system.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the cluster, which virtual machines are to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "List of virtual machines from given page of given size were found for given cluster in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No virtual machines was found in the oVirt system (which could be due to pagination) for given cluster, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Cluster with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

    @Operation(
        method = "GET", summary = "Get all networks, which certain cluster has the access to in the oVirt engine instance",
        description = "This endpoint can be used by the administrator to fetch all the networks, which as located in certain clusters datacenter in the oVirt system.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the cluster, which networks are to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "List of networks from given page of given size were found for given cluster in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No networks was found in the oVirt system (which could be due to pagination) for given cluster, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Cluster with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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

    @Operation(
        method = "GET", summary = "Get all events that occurred in certain cluster in the oVirt engine instance",
        description = "This endpoint can be used by the administrator to fetch all events that occurred in certain cluster in the oVirt system.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the cluster, which events are to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "List of events from given page of given size were found for given cluster in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No event was found in the oVirt system (which could be due to pagination) for given cluster, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Cluster with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
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
