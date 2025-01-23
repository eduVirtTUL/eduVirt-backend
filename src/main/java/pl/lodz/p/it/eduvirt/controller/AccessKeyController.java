package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import pl.lodz.p.it.eduvirt.dto.access_key.CourseAccessKeyDto;
import pl.lodz.p.it.eduvirt.dto.access_key.TeamAccessKeyDto;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.AccessKeyMapper;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;
import pl.lodz.p.it.eduvirt.service.CourseService;
import pl.lodz.p.it.eduvirt.service.TeamService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/access-keys")
@RequiredArgsConstructor
public class AccessKeyController {

    /* Services */

    private final AccessKeyService accessKeyService;
    private final CourseService courseService;
    private final TeamService teamService;

    /* Mappers */

    private final AccessKeyMapper accessKeyMapper;

    /* Repositories */

    private final UserRepository userRepository;


    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    @PostMapping("/course/{courseId}")
    public ResponseEntity<CourseAccessKeyDto> createCourseKey(@PathVariable UUID courseId, @RequestParam String courseKey) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            return ResponseEntity.ok(accessKeyMapper
                    .toCourseKeyDto(accessKeyService
                            .createCourseKey(course, courseKey)));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Transactional
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public ResponseEntity<CourseAccessKeyDto> getKeyForCourse(@PathVariable UUID courseId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Course course = courseService.getCourse(courseId);

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user))) {
            return ResponseEntity.ok(accessKeyMapper
                    .toCourseKeyDto(accessKeyService
                            .getKeyForCourse(course)));
        }

        return ResponseEntity.noContent().build();
    }

    @Transactional
    @GetMapping("/team/{teamId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TeamAccessKeyDto> getKeyForTeam(@PathVariable UUID teamId) {
        UUID userId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.toString()));
        Team team = teamService.getTeamById(teamId);
        Course course = team.getCourse();

        List<String> authorities = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        if (authorities.contains(RoleConstants.ADMINISTRATOR) ||
                (authorities.contains(RoleConstants.TEACHER) && course.getTeachers().contains(user)) ||
                (authorities.contains(RoleConstants.STUDENT) && team.getUsers().contains(user))) {
            return ResponseEntity.ok(accessKeyMapper
                    .toTeamKeyDto(accessKeyService
                            .getKeyForTeam(team, course)));
        }

        return ResponseEntity.noContent().build();
    }
}