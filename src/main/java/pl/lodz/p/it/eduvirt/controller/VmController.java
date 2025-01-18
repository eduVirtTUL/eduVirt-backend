package pl.lodz.p.it.eduvirt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDto;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmGeneralDto;
import pl.lodz.p.it.eduvirt.exceptions.handle.ExceptionResponse;
import pl.lodz.p.it.eduvirt.mappers.EventMapper;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@LoggerInterceptor
@RequestMapping("/resource/vm")
@RequiredArgsConstructor
public class VmController {

    /* Services */

    private final OVirtVmService oVirtVmService;
    private final OVirtClusterService oVirtClusterService;
    private final OVirtVnicProfileService oVirtVnicProfileService;

    /* Mappers */

    private final VmMapper vmMapper;
    private final EventMapper eventMapper;

    @GetMapping
    public ResponseEntity<List<VmDto>> getVms() {
        return ResponseEntity.ok(vmMapper.ovirtVmsToDtos(oVirtVmService.findVms().stream()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VmDto> getVm(@PathVariable String id) {
        Vm vm = oVirtVmService.findVmById(id);
        VmDto vmDto = vmMapper.ovirtVmToDto(vm);
        vmDto.setNics(
                vm.nics().parallelStream().map(nic -> {
                    NicDto.NicDtoBuilder nicDtoBuilder = NicDto.builder()
                            .id(nic.id())
                            .name(nic.name())
                            .macAddress(nic.mac().address());
                    if (nic.vnicProfilePresent()) {
                        VnicProfile profile = oVirtVnicProfileService.getVnicProfileById(nic.vnicProfile().id());
                        return nicDtoBuilder
                                .profileName(profile.name())
                                .build();
                    }

                    return nicDtoBuilder
                            .build();

                }).toList());
        return ResponseEntity.ok(vmDto);
    }

    @Operation(
        method = "GET", summary = "Get resources required by certain virtual machine",
        description = "This endpoint can be used to fetch cpus and memory that is required by certain virtual machine. In this case cpus are returned in pieces, and memory size is returned in bytes.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the virtual machine, which required resources are to be fetched from the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Virtual machine identified with given identifier was found in the oVirt system and its required cpus and memory size were sent to the client successfully."),
            @ApiResponse(responseCode = "404",
                description = "Virtual machine with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/required-resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResourcesDto> findVmRequiredResources(@PathVariable("id") UUID vmId) {
        Vm oVirtVM = oVirtVmService.findVmWithCpuProfileById(vmId.toString());

        Map<String, Object> requiredResources;
        if (oVirtVM.cpuProfile().qos() != null) {
            Qos vmCpuQos = oVirtVmService.findQosForVmCpu(oVirtVM);
            Cluster foundCluster = oVirtClusterService.findClusterById(UUID.fromString(oVirtVM.cluster().id()));
            List<Host> clusterHosts = oVirtClusterService.findAllHostsInCluster(foundCluster);
            requiredResources = oVirtVmService.findVmResources(oVirtVM, vmCpuQos, clusterHosts.getFirst(), foundCluster);
        } else {
            requiredResources = oVirtVmService.findVmResources(oVirtVM, null, null, null);
        }

        ResourcesDto resources = new ResourcesDto(
                (int) requiredResources.get("cpu"), (long) requiredResources.get("memory"));

        return ResponseEntity.ok(resources);
    }

    @Operation(
        method = "GET", summary = "Get all virtual machines that are located in certain cluster in the oVirt engine instance",
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
    @GetMapping(path = "/clusters/{clusterId}")
    public ResponseEntity<List<VmDto>> findVmsForCluster(@PathVariable("clusterId") UUID clusterId) {
        Cluster foundCluster = oVirtClusterService.findClusterById(clusterId);
        List<Vm> foundVms = oVirtVmService.findVmsForCluster(foundCluster);

        List<VmDto> listOfDTOs = foundVms.stream().map(vmMapper::ovirtVmToDto).toList();

        if (foundVms.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

    @Operation(
        method = "GET", summary = "Get all events that were registered for certain virtual machine in the oVirt engine instance",
        description = "This endpoint can be used to fetch all events that occurred in certain virtual machine in the oVirt system.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH, description = "Identifier of the virtual machines, which events are to be found in the oVirt system.", required = true),
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "List of events from given page of given size were found for given virtual machine in the oVirt system and sent to the client successfully."),
            @ApiResponse(responseCode = "204",
                description = "No event was found in the oVirt system (which could be due to pagination) for given virtual machine, and as a result 204 NO CONTENT is returned.",
                content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404",
                description = "Virtual machine with given identifier could not be found in the oVirt system or some error occurred during calls to the oVirt system and the data could not be retrieved.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
            @ApiResponse(responseCode = "500",
                description = "Some other, unknown error occurred while processing the request.",
                content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
        }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping(path = "/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EventGeneralDto>> findEventsForVm(
            @PathVariable("id") UUID vmId, @PageableDefault Pageable pageable) {
        Vm oVirtVM = oVirtVmService.findVmById(vmId.toString());
        List<Event> foundEvents = oVirtVmService.findEventsByVmId(oVirtVM, pageable);

        List<EventGeneralDto> listOfDTOs = foundEvents.stream()
                .map(eventMapper::ovirtEventToGeneralDTO).toList();

        if (foundEvents.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }
}
