package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreateStatelessPodDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodDetailsDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.mappers.PodMapper;
import pl.lodz.p.it.eduvirt.service.PodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods")
@RequiredArgsConstructor
public class PodController {

    private final PodService podService;
    private final PodMapper podMapper;

    //Stateful
    @PostMapping("/stateful")
    public ResponseEntity<PodStatefulDto> createStatefulPod(@RequestBody CreatePodStatefulDto createDto) {
        PodStateful createdPod = podService.createStatefulPod(createDto);
        return ResponseEntity.ok(podMapper.podStatefulToDto(createdPod));
    }

    @GetMapping("/stateful/{podId}")
    public ResponseEntity<PodDetailsDto> getStatefulPod(@PathVariable UUID podId) {
        return ResponseEntity.ok(podMapper.podStatefulToDetailsDto(podService.getStatefulPod(podId)));
    }

    @GetMapping("/stateful/team/{teamId}")
    public ResponseEntity<List<PodDetailsDto>> getStatefulPodsByTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(
                podService.getStatefulPodsByTeam(teamId).stream()
                        .map(podMapper::podStatefulToDetailsDto)
                        .toList()
        );
    }

    @GetMapping("/stateful/course/{courseId}")
    public ResponseEntity<List<PodStatefulDto>> getStatefulPodsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(
                podService.getStatefulPodsByCourse(courseId).stream()
                        .map(podMapper::podStatefulToDto)
                        .toList()
        );
    }

    @GetMapping("/stateful/resource-group/{resourceGroupId}")
    public ResponseEntity<List<PodStatefulDto>> getStatefulPodsByResourceGroup(@PathVariable UUID resourceGroupId) {
        return ResponseEntity.ok(
                podService.getStatefulPodsByResourceGroup(resourceGroupId).stream()
                        .map(podMapper::podStatefulToDto)
                        .toList()
        );
    }

    @DeleteMapping("/stateful/{podId}")
    public ResponseEntity<Void> deleteStatefulPod(@PathVariable UUID podId) {
        podService.deleteStatefulPod(podId);
        return ResponseEntity.noContent().build();
    }

    //Stateless
    @PostMapping("/stateless")
    public ResponseEntity<Void> createStatelessPod(@RequestBody CreateStatelessPodDto createDto) {
        podService.createStatelessPod(createDto.teamId(), createDto.resourceGroupPoolId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/stateless/{teamId}/{resourceGroupPoolId}")
    public ResponseEntity<Void> deleteStatelessPod(
            @PathVariable UUID teamId,
            @PathVariable UUID resourceGroupPoolId
    ) {
        podService.deleteStatelessPod(teamId, resourceGroupPoolId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stateless/team/{teamId}")
    public ResponseEntity<List<UUID>> getStatelessPodsByTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(podService.getStatelessPodsByTeam(teamId));
    }

}