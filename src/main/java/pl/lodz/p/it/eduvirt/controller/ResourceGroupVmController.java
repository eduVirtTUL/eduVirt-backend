package pl.lodz.p.it.eduvirt.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.AddVmDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.EditVmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
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
    private final VirtualMachineService virtualMachineService;
    private final VmMapper vmMapper;

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
    public ResponseEntity<Void> addVm(@PathVariable UUID rgId,
                                      @RequestBody AddVmDto addVmDto,
                                      @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);

        virtualMachineService.createVirtualMachine(addVmDto.id(), addVmDto.hidden(), resourceGroup, etag);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteVm(@PathVariable UUID rgId,
                                         @PathVariable UUID id,
                                         @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        virtualMachineService.deleteVirtualMachine(id, rgId, etag);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> updateVm(@PathVariable UUID rgId, @PathVariable UUID id, @RequestBody @Validated EditVmDto editVm) {
        virtualMachineService.updateVirtualMachine(id, editVm.hidden());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    public ResponseEntity<List<VmDto>> getAvailableVms(@PathVariable UUID rgId) {
        return ResponseEntity.ok(
                vmMapper.ovirtVmsToDtos(resourceGroupService.findAvailableVms(rgId).stream())
        );
    }
}
