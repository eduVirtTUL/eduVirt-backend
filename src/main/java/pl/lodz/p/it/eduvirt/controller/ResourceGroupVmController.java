package pl.lodz.p.it.eduvirt.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group.AddVmDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.EditVmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDtoWthEtag;
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
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public ResponseEntity<List<VmDto>> getVms(@PathVariable UUID rgId) {
        return ResponseEntity.ok(resourceGroupService.getVms(rgId));
    }

    @GetMapping("{id}")
    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public ResponseEntity<VmDto> getVm(@PathVariable UUID rgId, @PathVariable UUID id) {
        VmDtoWthEtag vm = resourceGroupService.getVm(id);
        return ResponseEntity.ok().eTag(vm.etag()).body(vm.vmDto());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> addVm(@PathVariable UUID rgId,
                                      @RequestBody AddVmDto addVmDto,
                                      @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        virtualMachineService.createVirtualMachine(rgId, addVmDto.id(), addVmDto.hidden(), etag);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> deleteVm(@PathVariable UUID rgId,
                                         @PathVariable UUID id,
                                         @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        virtualMachineService.deleteVirtualMachine(id, rgId, etag);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> updateVm(@PathVariable UUID rgId,
                                         @PathVariable UUID id,
                                         @RequestBody @Validated EditVmDto editVm,
                                         @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        virtualMachineService.updateVirtualMachine(id, editVm.hidden(), etag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<List<VmDto>> getAvailableVms(@PathVariable UUID rgId) {
        return ResponseEntity.ok(
                vmMapper.ovirtVmsToDtos(resourceGroupService.findAvailableVms(rgId).stream())
        );
    }
}
