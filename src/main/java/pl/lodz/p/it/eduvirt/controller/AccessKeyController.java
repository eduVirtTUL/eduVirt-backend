package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import pl.lodz.p.it.eduvirt.dto.access_key.AccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.mappers.AccessKeyMapper;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;

import java.util.UUID;

@RestController
@RequestMapping("/access-keys")
@RequiredArgsConstructor
public class AccessKeyController {

    private final AccessKeyService accessKeyService;
    private final AccessKeyMapper accessKeyMapper;

    @PostMapping("/course")
    public ResponseEntity<AccessKeyDto> createCourseKey(@RequestParam UUID courseId, @RequestParam String courseKey) {
        AccessKey key = accessKeyService.createCourseKey(courseId, courseKey);
        return ResponseEntity.ok(accessKeyMapper.toDto(key));
    }


    @GetMapping("/{keyValue}")
    public ResponseEntity<AccessKeyDto> getKey(@PathVariable String keyValue) {
        AccessKey key = accessKeyService.getKey(keyValue);
        return ResponseEntity.ok(accessKeyMapper.toDto(key));
    }

}