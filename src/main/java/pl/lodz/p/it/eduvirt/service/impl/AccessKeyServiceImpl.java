package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyLengthException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.DuplicateKeyValueException;
import pl.lodz.p.it.eduvirt.exceptions.course.InvalidCourseTypeException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;

import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessKeyServiceImpl implements AccessKeyService {

    /* Repositories */

    private final TeamRepository teamRepository;
    private final TeamAccessKeyRepository teamAccessKeyRepository;
    private final CourseAccessKeyRepository courseAccessKeyRepository;

    /* Constants */

    private static final String KEY_FORMAT_REGEX = "^[a-zA-Z0-9-_]{5,50}$";


    /* Helper methods */

    private String generateKeyValue(String baseName) {
        String baseKey = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 4);
        return baseKey.substring(0, Math.min(baseKey.length(), 4)) + randomPart;
    }

    private String validateAndGetKeyValue(String providedKey, String name,
                                          Predicate<String> existsCheck) {
        if (providedKey != null && !providedKey.isEmpty()) {
            if (!providedKey.matches(KEY_FORMAT_REGEX)) {
                throw new AccessKeyLengthException();
            }
            if (existsCheck.test(providedKey)) {
                throw new DuplicateKeyValueException(providedKey);
            }
            return providedKey;
        }

        String generatedKey;
        do {
            generatedKey = generateKeyValue(name);
        } while (existsCheck.test(generatedKey));

        return generatedKey;
    }

    /* Service methods */

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public CourseAccessKey createCourseKey(Course course, String userCourseKey) {
        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new InvalidCourseTypeException("Cannot create access key for a team based course");
        }

        if (courseAccessKeyRepository.existsByCourseId(course.getId())) {
            throw new AccessKeyAlreadyExistsException();
        }

        String keyValue = validateAndGetKeyValue(userCourseKey, course.getName(),
                courseAccessKeyRepository::existsByKeyValue);

        CourseAccessKey newCourseAccessKey = new CourseAccessKey();
        newCourseAccessKey.setKeyValue(keyValue);
        newCourseAccessKey.setCourse(course);

        return courseAccessKeyRepository.saveAndFlush(newCourseAccessKey);
    }

    @Override
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public CourseAccessKey getKeyForCourse(Course course) {
        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new InvalidCourseTypeException("Cannot get access key for a team based course");
        }

        return courseAccessKeyRepository.findByCourseId(course.getId())
                .orElseThrow(AccessKeyNotFoundException::new);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void createTeamKey(UUID teamId, String teamKey) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (teamAccessKeyRepository.existsByTeamId(teamId)) {
            throw new AccessKeyAlreadyExistsException();
        }

        String keyValue = validateAndGetKeyValue(teamKey, team.getName(),
                teamAccessKeyRepository::existsByKeyValue);

        TeamAccessKey newTeamAccessKey = new TeamAccessKey();
        newTeamAccessKey.setKeyValue(keyValue);
        newTeamAccessKey.setTeam(team);

        teamAccessKeyRepository.saveAndFlush(newTeamAccessKey);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TeamAccessKey getKeyForTeam(Team team, Course course) {
        if (course.getCourseType() == CourseType.SOLO) {
            throw new InvalidCourseTypeException("Cannot get access key to a team in a solo course");
        } else {
            return teamAccessKeyRepository.findByTeamId(team.getId())
                    .orElseThrow(AccessKeyNotFoundException::new);
        }
    }
}