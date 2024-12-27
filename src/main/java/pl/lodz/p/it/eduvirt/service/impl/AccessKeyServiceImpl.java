package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.AccessKeyType;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.AccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessKeyServiceImpl implements AccessKeyService {

    private final AccessKeyRepository accessKeyRepository;
    private final CourseRepository courseRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public AccessKey createCourseKey(UUID courseId, String userCourseKey) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IllegalStateException("Cannot create access key for a team based course");
        }

        if (accessKeyRepository.existsByCourseIdAndAccessKeyType(courseId, AccessKeyType.COURSE)) {
            throw new IllegalStateException("Course already has an access key");
        }

        String keyValue;
        if (userCourseKey != null && !userCourseKey.isEmpty()) {
            if (userCourseKey.length() < 5) {
                throw new IllegalArgumentException("Key must be at least 5 characters long");
            }
            if (userCourseKey.length() > 15) {
                throw new IllegalArgumentException("Key must be at most 15 characters long");
            }
            keyValue = userCourseKey;
        } else {
            do {
                keyValue = generateKeyValue(course.getName());
            } while (accessKeyRepository.existsByKeyValue(keyValue));
        }

        AccessKey accessKey = AccessKey.builder()
                .keyValue(keyValue)
                .accessKeyType(AccessKeyType.COURSE)
                .course(course)
                .build();

        return accessKeyRepository.save(accessKey);
    }

    //this method is most likely redundant; will keep it for now
    @Override
    @Transactional
    public AccessKey createTeamKey(UUID teamId, String userTeamKey) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (accessKeyRepository.existsByTeamId(teamId)) {
            throw new IllegalStateException("Team already has an access key");
        }

        String keyValue;
        if (userTeamKey != null && !userTeamKey.isEmpty()) {
            if (userTeamKey.length() < 5) {
                throw new IllegalArgumentException("Key must be at least 5 characters long");
            }
            if (userTeamKey.length() > 15) {
                throw new IllegalArgumentException("Key must be at most 15 characters long");
            }
            keyValue = userTeamKey;
        } else {
            do {
                keyValue = generateKeyValue(team.getName());
            } while (accessKeyRepository.existsByKeyValue(keyValue));
        }

        AccessKey accessKey = AccessKey.builder()
                .keyValue(keyValue)
                .accessKeyType(AccessKeyType.TEAM)
                .course(team.getCourse())
                .team(team)
                .build();

        return accessKeyRepository.save(accessKey);
    }

    private String generateKeyValue(String baseName) {
        String baseKey = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 4);
        return baseKey.substring(0, Math.min(baseKey.length(), 4)) + randomPart;
    }

    @Override
    public AccessKey getKey(String keyValue) {
        return accessKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(() -> new RuntimeException("Key not found"));
    }
}