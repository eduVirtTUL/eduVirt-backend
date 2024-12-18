package pl.lodz.p.it.eduvirt.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.AddVmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;
import pl.lodz.p.it.eduvirt.service.VirtualMachineService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/resource-group/{rgId}/vm")
@RequiredArgsConstructor
public class ResourceGroupVmController {
    private final ResourceGroupService resourceGroupService;
    private final OVirtVmService oVirtVmService;
    private final VirtualMachineService virtualMachineService;
    private final OVirtVnicProfileService oVirtVnicProfileService;

    @GetMapping
    @Transactional
    public ResponseEntity<List<VmDto>> getVms(@PathVariable UUID rgId) {
        return ResponseEntity.ok(resourceGroupService.getVms(rgId));
    }

    @GetMapping("{id}")
    @Transactional
    public ResponseEntity<VmDto> getVm(@PathVariable UUID rgId, @PathVariable UUID id) {
        return ResponseEntity.ok(resourceGroupService.getVm(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> addVm(@PathVariable UUID rgId, @RequestBody AddVmDto addVmDto) {
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);

        virtualMachineService.createVirtualMachine(addVmDto.id(), resourceGroup);
        return ResponseEntity.ok().build();
    }
}
