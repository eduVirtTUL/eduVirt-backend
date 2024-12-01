package pl.lodz.p.it.eduvirt.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.dto.resource_group.AddVmDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group/{rgId}/vm")
@RequiredArgsConstructor
public class ResourceGroupVmController {
    private final ResourceGroupService resourceGroupService;
    private final OVirtVmService oVirtVmService;
    private final VmMapper vmMapper;

    @GetMapping
    @Transactional
    public ResponseEntity<List<VmDto>> getVms(@PathVariable UUID rgId) {
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);
        var vms = resourceGroup.getVms()
                .stream()
                .parallel()
                .map(vm -> oVirtVmService.findVmById(vm.toString()));

        return ResponseEntity.ok(vmMapper.ovirtVmsToDtos(vms));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> addVm(@PathVariable UUID rgId, @RequestBody AddVmDto addVmDto) {
        ResourceGroup resourceGroup = resourceGroupService.getResourceGroup(rgId);
        resourceGroup.getVms().add(addVmDto.id());
        return ResponseEntity.ok().build();
    }
}
