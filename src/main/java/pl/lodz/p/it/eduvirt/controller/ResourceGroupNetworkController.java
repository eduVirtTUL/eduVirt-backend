package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.CreateResourceGroupNetworkDto;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.ResourceGroupNetworkDto;
import pl.lodz.p.it.eduvirt.mappers.ResourceGroupNetworkMapper;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resource-group/{rgId}/network")
@RequiredArgsConstructor
public class ResourceGroupNetworkController {
    private final ResourceGroupNetworkMapper resourceGroupNetworkMapper;
    private final ResourceGroupNetworkService resourceGroupNetworkService;

    @PostMapping
    public ResponseEntity<ResourceGroupNetworkDto> addResourceGroupNetwork(
            CreateResourceGroupNetworkDto resourceGroupNetworkDto, @PathVariable UUID rgId) {
        return ResponseEntity.ok(
                resourceGroupNetworkMapper.toResourceGroupNetworkDto(
                        resourceGroupNetworkService.addResourceGroupNetwork(rgId, resourceGroupNetworkDto.name())
                )
        );
    }

    @GetMapping
    public ResponseEntity<List<ResourceGroupNetworkDto>> get(@PathVariable UUID rgId) {
        return ResponseEntity.ok(
                resourceGroupNetworkMapper.toResourceGroupNetworkDtos(
                        resourceGroupNetworkService.getResourceGroupNetworks(rgId).stream()
                )
        );
    }
}
