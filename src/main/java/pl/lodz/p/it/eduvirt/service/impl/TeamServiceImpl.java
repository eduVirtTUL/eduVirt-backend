package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.DuplicateAccessKeyValueException;
import pl.lodz.p.it.eduvirt.exceptions.access_key.InvalidKeyTypeException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.service.TeamService;

import java.util.List;
import java.util.UUID;

import static pl.lodz.p.it.eduvirt.entity.key.AccessKeyType.TEAM;


@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final AccessKeyRepository accessKeyRepository;

    private void validateUserNotInCourse(UUID userId, UUID courseId) {
        if (teamRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new UserAlreadyInCourseException();
        }
    }

    private void validateTeamNameUnique(String name, UUID courseId) {
        boolean exists = teamRepository.existsByNameAndCourseId(name, courseId);
        if (exists) {
            throw new TeamAlreadyExistsException();
        }
    }

    @Override
    @Transactional
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional
    public Team getTeamById(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);
    }

    @Override
    public List<Team> getTeamsByUser(UUID userId) {
        return teamRepository.findByUsersContains(userId);
    }

    @Override
    @Transactional
    public List<Team> getTeamsByCourse(UUID courseId) {
        return teamRepository.findByCourses(courseId);
    }

    //TODO: add logic later to check whether the user is a teacher/student/admin
    @Override
    public Team addUserToTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        validateUserNotInCourse(userId, team.getCourse().getId());

        if (team.getUsers().size() >= team.getMaxSize()) {
            throw new TeamSizeException();
        }

        team.getUsers().add(userId);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team createTeam(Team team, UUID courseId, String userKeyValue) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.SOLO) {
            throw new IncorrectTeamTypeException();
        }

        if (team.getMaxSize() < 1 || team.getMaxSize() > 8) {
            throw new TeamSizeException();
        }

        validateTeamNameUnique(team.getName(), courseId);

        team.setCourse(course);
        team.setActive(true);
        team = teamRepository.save(team);

        AccessKey accessKey = new AccessKey();
        String keyValue;
        if (userKeyValue != null && userKeyValue.length() > 5 && userKeyValue.length() < 15) {
            if (accessKeyRepository.findByKeyValue(userKeyValue).isPresent()) {
                throw new DuplicateAccessKeyValueException();
            }
            keyValue = userKeyValue;
        } else {
            do {
                keyValue = UUID.randomUUID().toString().substring(0, 8);
            } while (accessKeyRepository.findByKeyValue(keyValue).isPresent());
        }

        accessKey.setKeyValue(keyValue);
        accessKey.setTeam(team);
        accessKey.setCourse(course);
        accessKey.setAccessKeyType(TEAM);
        accessKeyRepository.save(accessKey);

        team.setAccessKey(accessKey);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team joinTeamOrCourse(String keyValue, UUID userId) {
        AccessKey key = accessKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(AccessKeyNotFoundException::new);

        switch (key.getAccessKeyType()) {
            case TEAM -> {
                return handleTeamJoin(key, userId);
            }
            case COURSE -> {
                return handleCourseJoin(key, userId);
            }
            default -> throw new InvalidKeyTypeException();
        }
    }

    private Team handleTeamJoin(AccessKey key, UUID userId) {
        Team team = key.getTeam();

        validateUserNotInCourse(userId, team.getCourse().getId());
        if (team.getUsers().size() >= team.getMaxSize()) {
            throw new TeamSizeException();
        }

        team.getUsers().add(userId);
        return teamRepository.save(team);
    }

    private Team handleCourseJoin(AccessKey key, UUID userId) {
        Course course = key.getCourse();
        validateUserNotInCourse(userId, course.getId());

        Long soloTeamCount = teamRepository.countByCourseId(course.getId());
        String teamName = course.getName() + " - Solo " + (soloTeamCount + 1);

        Team newTeam = Team.builder()
                .name(teamName)
                .course(course)
                .users(List.of(userId))
                .maxSize(1)
                .active(true)
                .build();
        return teamRepository.save(newTeam);
    }

    @Override
    public Team removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        if (team.getUsers().contains(userId)) {
            team.getUsers().remove(userId);
            return teamRepository.save(team);

        } else {
            throw new UserNotFoundException();
        }
    }


}