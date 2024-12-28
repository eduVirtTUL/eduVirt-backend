package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.CreateStatelessPodDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.PodStateful;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;
import pl.lodz.p.it.eduvirt.service.PodService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods")
@RequiredArgsConstructor
public class PodController {

    private final PodService podService;
    private final PodStatefulMapper podStatefulMapper;

    //Stateful
    @PostMapping("/stateful")
    public ResponseEntity<PodStatefulDto> createPod(@RequestBody CreatePodStatefulDto createDto) {
//        PodStateful pod = podStatefulMapper.toEntity(createDto);
        PodStateful createdPod = podService.createPod(createDto);
        return ResponseEntity.ok(podStatefulMapper.toDto(createdPod));
    }

    @GetMapping("/stateful/team/{teamId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(
                podService.getPodsByTeam(teamId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/stateful/course/{courseId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(
                podService.getPodsByCourse(courseId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/stateful/resource-group/{resourceGroupId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByResourceGroup(@PathVariable UUID resourceGroupId) {
        return ResponseEntity.ok(
                podService.getPodsByResourceGroup(resourceGroupId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @DeleteMapping("/stateful/{podId}")
    public ResponseEntity<Void> deletePod(@PathVariable UUID podId) {
        podService.deletePod(podId);
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