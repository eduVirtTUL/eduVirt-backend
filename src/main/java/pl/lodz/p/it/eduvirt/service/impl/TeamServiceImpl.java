package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.exceptions.user.UserNotFoundException;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;
import pl.lodz.p.it.eduvirt.service.TeamService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final TeamAccessKeyRepository teamKeyRepository;
    private final CourseAccessKeyRepository courseKeyRepository;
    private final AccessKeyService accessKeyService;

    private void validateUserNotInCourse(UUID userId, UUID courseId) {
        if (teamRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new UserAlreadyInCourseException();
        }
    }

    private void validateUserNotInTeam(Team team, UUID userId) {
        if (team.getUsers().contains(userId)) {
            throw new UserAlreadyInTeamException();
        }
    }

    private void validateTeamSizeAndName(Team team, UUID courseId) {
        if (team.getMaxSize() < 1 || team.getMaxSize() > 8) {
            throw new TeamSizeException();
        }

        if (teamRepository.existsByNameAndCourseId(team.getName(), courseId)) {
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

    @Override
    @Transactional
    public Team createTeam(Team team, UUID courseId, String userKeyValue) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.SOLO) {
            throw new IncorrectTeamTypeException();
        }

        validateTeamSizeAndName(team, courseId);
        team.setCourse(course);
        team.setActive(true);

        accessKeyService.createTeamKey(team.getId(), userKeyValue);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public void joinUsingKey(String keyValue, UUID userId) {
        try {
            teamKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);
            addUserToTeam(keyValue, userId);
            return;
        } catch (AccessKeyNotFoundException ignored) {
        }
        try {
            courseKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);
            addUserToCourse(keyValue, userId);
        } catch (AccessKeyNotFoundException e) {
            throw new AccessKeyNotFoundException();
        }
    }

    @Override
    @Transactional
    public void addUserToTeam(String keyValue, UUID userId) {
        TeamAccessKey key = teamKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(AccessKeyNotFoundException::new);

        Team team = key.getTeam();
        validateUserNotInTeam(team, userId);

        team.getUsers().add(userId);
        teamRepository.save(team);
    }

    @Override
    @Transactional
    public void addUserToCourse(String keyValue, UUID userId) {
        CourseAccessKey key = courseKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(AccessKeyNotFoundException::new);

        Course course = key.getCourse();
        validateUserNotInCourse(userId, course.getId());

        createSoloTeam(course.getId(), userId);
    }

    @Override
    @Transactional
    public void createSoloTeam(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() != CourseType.SOLO) {
            throw new IncorrectTeamTypeException();
        }

        Long soloTeamCount = teamRepository.countByCourseId(courseId);
        String teamName = course.getName() + " - Solo " + (soloTeamCount + 1);

        Team team = Team.builder()
                .name(teamName)
                .course(course)
                .users(List.of(userId))
                .maxSize(1)
                .active(true)
                .build();

        teamRepository.save(team);
    }

    @Override
    public void removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        if (team.getUsers().contains(userId)) {
            team.getUsers().remove(userId);
            teamRepository.save(team);

        } else {
            throw new UserNotFoundException();
        }
    }
}