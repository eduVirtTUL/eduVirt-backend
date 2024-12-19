package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.NetworkVmConnectionDto;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.UUID;

@RestController
@RequestMapping("/network")
@RequiredArgsConstructor
public class PrivateNetworkController {
    private final ResourceGroupNetworkService resourceGroupNetworkService;

    @PostMapping("/{id}/attach")
    public ResponseEntity<Void> attachNicToNetwork(@PathVariable UUID id, @RequestBody NetworkVmConnectionDto dto) {
        resourceGroupNetworkService.attachNicToNetwork(id, dto.vmId(), dto.nicId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detach")
    public ResponseEntity<Void> detachNicFromNetwork(@RequestBody NetworkVmConnectionDto dto) {
        resourceGroupNetworkService.detachNicFromNetwork(dto.vmId(), dto.nicId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNetwork(@PathVariable UUID id) {
        resourceGroupNetworkService.deleteNetwork(id);
        return ResponseEntity.ok().build();
    }
}
