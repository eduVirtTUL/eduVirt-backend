package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.dto.resource_group.ResourceGroupDto;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group")
@RequiredArgsConstructor
public class ResourceGroupController {

    private final ResourceGroupService resourceGroupService;
    private final ResourceGroupMapper resourceGroupMapper;

    @GetMapping
    public ResponseEntity<List<ResourceGroupDto>> getResourceGroups() {
        return ResponseEntity.ok(resourceGroupMapper.toDtos(resourceGroupService.getResourceGroups().stream()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceGroupDto> getResourceGroup(@PathVariable UUID id) {
        return ResponseEntity.ok(resourceGroupMapper.toDto(resourceGroupService.getResourceGroup(id)));
    }
}