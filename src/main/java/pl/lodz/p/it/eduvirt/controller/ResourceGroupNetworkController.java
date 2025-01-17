package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<ResourceGroupNetworkDto> addResourceGroupNetwork(@PathVariable UUID rgId,
                                                                           @RequestBody CreateResourceGroupNetworkDto resourceGroupNetworkDto,
                                                                           @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        return ResponseEntity.ok(
                resourceGroupNetworkMapper.toResourceGroupNetworkDto(
                        resourceGroupNetworkService.addResourceGroupNetwork(rgId, resourceGroupNetworkDto.name(), etag)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    @Transactional
    public ResponseEntity<List<ResourceGroupNetworkDto>> get(@PathVariable UUID rgId) {
        return ResponseEntity.ok(
                resourceGroupNetworkMapper.toResourceGroupNetworkDtos(
                        resourceGroupNetworkService.getResourceGroupNetworks(rgId).stream()
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('teacher')")
    public ResponseEntity<Void> deleteNetwork(@PathVariable UUID id,
                                              @PathVariable UUID rgId,
                                              @RequestHeader(HttpHeaders.IF_MATCH) String etag) {
        resourceGroupNetworkService.deleteNetwork(id, rgId, etag);
        return ResponseEntity.ok().build();
    }
}
