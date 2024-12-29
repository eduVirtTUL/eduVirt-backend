package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.lodz.p.it.eduvirt.dto.access_key.CourseAccessKeyDto;
import pl.lodz.p.it.eduvirt.dto.access_key.TeamAccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
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
    public ResponseEntity<CourseAccessKeyDto> createCourseKey(@PathVariable UUID courseId, @RequestParam String courseKey) {
        CourseAccessKey key = accessKeyService.createCourseKey(courseId, courseKey);
        return ResponseEntity.ok(accessKeyMapper.toCourseKeyDto(key));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseAccessKeyDto> getKeyForCourse(@PathVariable UUID courseId) {
        try {
            CourseAccessKey key = accessKeyService.getKeyForCourse(courseId);
            return ResponseEntity.ok(accessKeyMapper.toCourseKeyDto(key));
        } catch (AccessKeyNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/team/{teamId}")
    public ResponseEntity<TeamAccessKeyDto> getKeyForTeam(@PathVariable UUID teamId) {
        try {
            TeamAccessKey key = accessKeyService.getKeyForTeam(teamId);
            return ResponseEntity.ok(accessKeyMapper.toTeamKeyDto(key));
        } catch (AccessKeyNotFoundException e) {
            return ResponseEntity.noContent().build();
        }
    }

    // enhance this with etag later
    @PutMapping("/course/{courseId}")
    public ResponseEntity<CourseAccessKeyDto> updateCourseKey(@PathVariable UUID courseId, @RequestParam String courseKey) {
        CourseAccessKey key = accessKeyService.updateCourseKey(courseId, courseKey);
        return ResponseEntity.ok(accessKeyMapper.toCourseKeyDto(key));
    }
}