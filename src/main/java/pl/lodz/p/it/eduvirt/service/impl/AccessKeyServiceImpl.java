package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.ApplicationBaseException;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyLengthException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.TeamRepository;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessKeyServiceImpl implements AccessKeyService {

    private final CourseRepository courseRepository;
    private final TeamRepository teamRepository;
    private final TeamAccessKeyRepository teamAccessKeyRepository;
    private final CourseAccessKeyRepository courseAccessKeyRepository;

    private String generateKeyValue(String baseName) {
        String baseKey = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        String randomPart = UUID.randomUUID().toString().substring(0, 4);
        return baseKey.substring(0, Math.min(baseKey.length(), 4)) + randomPart;
    }

    private String validateAndGetKeyValue(String providedKey, String name,
                                          Predicate<String> existsCheck) {
        if (providedKey != null && !providedKey.isEmpty()) {
            if (providedKey.length() < 5 || providedKey.length() > 50) {
                throw new AccessKeyLengthException();
            }
            return providedKey;
        }

        String generatedKey;
        do {
            generatedKey = generateKeyValue(name);
        } while (existsCheck.test(generatedKey));

        return generatedKey;
    }

    @Override
    public CourseAccessKey createCourseKey(UUID courseId, String userCourseKey) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new ApplicationBaseException("Cannot create access key for a team based course");
        }

        if (courseAccessKeyRepository.existsByCourseId(courseId)) {
            throw new AccessKeyAlreadyExistsException();
        }

        String keyValue = validateAndGetKeyValue(userCourseKey, course.getName(),
                courseAccessKeyRepository::existsByKeyValue);

        CourseAccessKey newCourseAccessKey = new CourseAccessKey();
        newCourseAccessKey.setKeyValue(keyValue);
        newCourseAccessKey.setCourse(course);

        return courseAccessKeyRepository.save(newCourseAccessKey);
    }

    @Override
    public void createTeamKey(UUID teamId, String teamKey) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        if (teamAccessKeyRepository.existsByTeamId(teamId)) {
            throw new AccessKeyAlreadyExistsException();
        }

        String keyValue = validateAndGetKeyValue(teamKey, team.getName(),
                teamAccessKeyRepository::existsByKeyValue);

        TeamAccessKey newTeamAccessKey = new TeamAccessKey();
        newTeamAccessKey.setKeyValue(keyValue);
        newTeamAccessKey.setTeam(team);
        newTeamAccessKey.setCourse(team.getCourse());

        teamAccessKeyRepository.save(newTeamAccessKey);
    }

    @Override
    public CourseAccessKey getKeyForCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IllegalStateException("Cannot get access key for a team based course"); //TODO:
            // change to custom exception
        } else {
            return courseAccessKeyRepository.findByCourseId(courseId)
                    .orElseThrow(AccessKeyNotFoundException::new);
        }
    }

    @Override
    public TeamAccessKey getKeyForTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
        Course course = team.getCourse();

        if (course.getCourseType() == CourseType.SOLO) {
            throw new IllegalStateException("Cannot get access key to a team in a solo course"); //TODO:
            // change to custom exception
        } else {
            return teamAccessKeyRepository.findByTeamId(teamId)
                    .orElseThrow(AccessKeyNotFoundException::new);
        }
    }


    //TODO: add etag shenanigans later
    @Override
    public CourseAccessKey updateCourseKey(UUID courseId, String courseKey) {
        courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        CourseAccessKey accessKey = courseAccessKeyRepository.findByCourseId(courseId)
                .orElseThrow(AccessKeyNotFoundException::new);

        accessKey.setKeyValue(courseKey);
        return courseAccessKeyRepository.save(accessKey);
    }

}