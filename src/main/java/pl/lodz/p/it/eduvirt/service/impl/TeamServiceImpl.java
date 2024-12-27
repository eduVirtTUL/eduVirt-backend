package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.DuplicateKeyValueException;
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
            throw new IllegalStateException("User already has a team in this course");
        }
    }
    
    private void validateTeamNameUnique(String name, UUID courseId) {
        boolean exists = teamRepository.existsByNameAndCourseId(name, courseId);
        if (exists) {
            throw new IllegalStateException("Team name already exists in this course");
        }
    }

    @Override
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Team getTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
    }

    @Override
    public List<Team> getTeamsByUser(UUID userId) {
        return teamRepository.findByUsersContains(userId);
    }

    @Override
    @Transactional
    public List<Team> getTeamsByCourse(UUID courseId) {
        return teamRepository.findByCourses_IdWithFetch(courseId);
    }

    @Override
    public Team addUserToTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        validateUserNotInCourse(userId, team.getCourse().getId());

        if (team.getUsers().size() >= team.getMaxSize()) {
            throw new IllegalStateException("Team is full");
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
            throw new IllegalStateException("Cannot manually create teams in non-team based courses");
        }

        if (team.getMaxSize() < 1 || team.getMaxSize() > 8) {
            throw new IllegalArgumentException("Team size must be between 1 and 8");
        }

        validateTeamNameUnique(team.getName(), courseId);

        team.setCourse(course);
        team.setActive(true);
        team = teamRepository.save(team);

        AccessKey accessKey = new AccessKey();
        String keyValue;
        if (userKeyValue != null && userKeyValue.length() > 5 && userKeyValue.length() < 15) {
            if (accessKeyRepository.findByKeyValue(userKeyValue).isPresent()) {
                throw new DuplicateKeyValueException(userKeyValue);
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
        
        team.getKeys().add(accessKey);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team joinTeamOrCreate(String keyValue, UUID userId) {
        AccessKey key = accessKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(() -> new RuntimeException("Key not found"));

        switch (key.getAccessKeyType()) {
            case TEAM -> {
                return handleTeamJoin(key, userId);
            }
            case COURSE -> {
                return handleCourseJoin(key, userId);
            }
            default -> throw new IllegalArgumentException("Invalid key type");
        }
    }

    private Team handleTeamJoin(AccessKey key, UUID userId) {
        Team team = key.getTeam();
        validateUserNotInCourse(userId, team.getCourse().getId());
        if (team.getUsers().size() >= team.getMaxSize()) {
            throw new IllegalStateException("Team is full");
        }
        team.getUsers().add(userId);
        return teamRepository.save(team);
    }

    private Team handleCourseJoin(AccessKey key, UUID userId) {
        Course course = key.getCourse();
        validateUserNotInCourse(userId, course.getId());
        
        Long soloTeamCount = teamRepository.countByCourseId(course.getId());
        String teamName = course.getName() + " - Solo Team " + (soloTeamCount + 1);

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
                .orElseThrow(() -> new RuntimeException("Team not found"));

        team.getUsers().remove(userId);
        return teamRepository.save(team);
    }
}