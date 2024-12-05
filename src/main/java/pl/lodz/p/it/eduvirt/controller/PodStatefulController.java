package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.it.eduvirt.dto.pod.CreatePodStatefulDto;
import pl.lodz.p.it.eduvirt.dto.pod.PodStatefulDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.PodStateful;
import pl.lodz.p.it.eduvirt.mappers.PodStatefulMapper;
import pl.lodz.p.it.eduvirt.service.PodStatefulService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pods/stateful")
@RequiredArgsConstructor
public class PodStatefulController {

    private final PodStatefulService podStatefulService;
    private final PodStatefulMapper podStatefulMapper;

    @PostMapping
    public ResponseEntity<PodStatefulDto> createPod(@RequestBody CreatePodStatefulDto createDto) {
        PodStateful pod = podStatefulMapper.toEntity(createDto);
        PodStateful createdPod = podStatefulService.createPod(pod);
        return ResponseEntity.ok(podStatefulMapper.toDto(createdPod));
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(
                podStatefulService.getPodsByTeam(teamId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(
                podStatefulService.getPodsByCourse(courseId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/resource-group/{resourceGroupId}")
    public ResponseEntity<List<PodStatefulDto>> getPodsByResourceGroup(@PathVariable UUID resourceGroupId) {
        return ResponseEntity.ok(
                podStatefulService.getPodsByResourceGroup(resourceGroupId).stream()
                        .map(podStatefulMapper::toDto)
                        .toList()
        );
    }

    @DeleteMapping("/{podId}")
    public ResponseEntity<Void> deletePod(@PathVariable UUID podId) {
        podStatefulService.deletePod(podId);
        return ResponseEntity.noContent().build();
    }
}