package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.Cluster;
import org.ovirt.engine.sdk4.types.Host;
import org.ovirt.engine.sdk4.types.Vm;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.nic.NicDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
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
    private final VmMapper vmMapper;

    private final ConnectionFactory connectionFactory;

    private final OVirtVmService oVirtVmService;
    private final OVirtClusterService oVirtClusterService;
    private final OVirtVnicProfileService oVirtVnicProfileService;

    private final VmMapper vmMapper;

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

    @GetMapping(path = "/{id}/required-resources", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResourcesDto> findVmRequiredResources(@PathVariable("id") UUID vmId) {
        Vm oVirtVM = oVirtVmService.findVmById(vmId.toString());
        Cluster foundCluster = oVirtClusterService.findClusterById(UUID.fromString(oVirtVM.cluster().id()));
        List<Host> clusterHosts = oVirtClusterService.findAllHostsInCluster(foundCluster);

        Map<String, Object> requiredResources = oVirtVmService.findVmResources(oVirtVM, clusterHosts.getFirst(), foundCluster);

        ResourcesDto resources = new ResourcesDto(
                (int) requiredResources.get("cpu"),
                (long) requiredResources.get("memory")
        );

        return ResponseEntity.ok(resources);
    }
}
