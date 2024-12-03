package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.resource_group_network.NetworkVmConnectionDto;
import pl.lodz.p.it.eduvirt.service.ResourceGroupNetworkService;

import java.util.UUID;

@RestController
@RequestMapping("/network/{id}")
@RequiredArgsConstructor
public class PrivateNetworkController {
    private final ResourceGroupNetworkService resourceGroupNetworkService;

    @PostMapping("/attach")
    public ResponseEntity<Void> attachVmToNetwork(@PathVariable UUID id, @RequestBody NetworkVmConnectionDto dto) {
        resourceGroupNetworkService.attachVmToNetwork(id, dto.vmId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/detach")
    public ResponseEntity<Void> detachVmFromNetwork(@PathVariable UUID id, @RequestBody NetworkVmConnectionDto dto) {
        resourceGroupNetworkService.detachVmFromNetwork(id, dto.vmId());
        return ResponseEntity.ok().build();
    }
}
