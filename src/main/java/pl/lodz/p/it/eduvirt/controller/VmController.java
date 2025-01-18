package pl.lodz.p.it.eduvirt.controller;

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

    @PreAuthorize("hasAuthority('administrator')")
    @GetMapping(path = "/clusters/{clusterId}")
    public ResponseEntity<List<VmDto>> findVmsForCluster(@PathVariable("clusterId") UUID clusterId) {
        Cluster foundCluster = oVirtClusterService.findClusterById(clusterId);
        List<Vm> foundVms = oVirtVmService.findVmsForCluster(foundCluster);

        List<VmDto> listOfDTOs = foundVms.stream().map(vmMapper::ovirtVmToDto).toList();

        if (foundVms.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(listOfDTOs);
    }

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
