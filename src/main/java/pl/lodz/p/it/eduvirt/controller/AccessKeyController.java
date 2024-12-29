package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.lodz.p.it.eduvirt.dto.access_key.AccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.AccessKeyMapper;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;

import java.util.UUID;

@RestController
@RequestMapping("/access-keys")
@RequiredArgsConstructor
public class AccessKeyController {

    private final AccessKeyService accessKeyService;
    private final AccessKeyMapper accessKeyMapper;

    @PostMapping("/course/{courseId}")
    public ResponseEntity<AccessKeyDto> createCourseKey(@PathVariable UUID courseId, @RequestParam String courseKey) {
        AccessKey key = accessKeyService.createCourseKey(courseId, courseKey);
        return ResponseEntity.ok(accessKeyMapper.toDto(key));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<AccessKeyDto> getKeyForCourse(@PathVariable UUID courseId) {
        try {
            AccessKey key = accessKeyService.getKeyForCourse(courseId);
            return ResponseEntity.ok(accessKeyMapper.toDto(key));
        } catch (AccessKeyNotFoundException e) {
            return ResponseEntity.noContent().build();
        }

    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<AccessKeyDto> getKeyForTeam(@PathVariable UUID teamId) {
        try {
            AccessKey key = accessKeyService.getKeyForTeam(teamId);
            return ResponseEntity.ok(accessKeyMapper.toDto(key));
        } catch (AccessKeyNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    // enhance this with etag later
    @PutMapping("/course/{courseId}")
    public ResponseEntity<AccessKeyDto> updateCourseKey(@PathVariable UUID courseId, @RequestParam String courseKey) {
        AccessKey key = accessKeyService.updateCourseKey(courseId, courseKey);
        return ResponseEntity.ok(accessKeyMapper.toDto(key));
    }
}