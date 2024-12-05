package pl.lodz.p.it.eduvirt.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Course;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;
import pl.lodz.p.it.eduvirt.exceptions.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.repository.eduvirt.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.eduvirt.TeamRepository;
import pl.lodz.p.it.eduvirt.service.TeamService;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;

    private void validateUserNotInCourse(UUID userId, UUID courseId) {
        if (teamRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new IllegalStateException("User already has a team in this course"); // change to custom exception
        }
    }

    @Override
    @Transactional
    public Team createTeam(Team team, UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (!course.isTeamBased()) {
            throw new IllegalStateException("Cannot manually create teams in non-team based courses"); //TODO change to custom exception
        }

        if (team.getMaxSize() < 1 || team.getMaxSize() > 8) {
            throw new IllegalArgumentException("Team size must be between 1 and 8"); //TODO change to custom exception
        }

        String userKey = team.getKey();
        if (userKey == null || userKey.length() < 4) {
            throw new IllegalArgumentException("Team key must be at least 4 characters"); //TODO change to custom exception
        }

        team.setKey("t" + userKey);
        if (teamRepository.existsByKey(team.getKey())) {
            throw new IllegalStateException("Team with this key already exists"); //TODO change to custom exception
        }

        team.setCourse(course);
        team.setActive(true);

        return teamRepository.save(team);
    }

    @Override
    public List<Team> getTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Team getTeam(UUID teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found")); //TODO change to custom exception
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
            throw new IllegalStateException("Team is full"); // change to custom exception
        }

        team.getUsers().add(userId);
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public Team joinTeamOrCreate(String key, UUID userId) {
        if (key.startsWith("t")) {
            Team team = teamRepository.findByKey(key)
                    .orElseThrow(() -> new RuntimeException("Team not found with key: " + key)); // change to custom exception

            validateUserNotInCourse(userId, team.getCourse().getId());

            if (team.getUsers().size() >= team.getMaxSize()) {
                throw new IllegalStateException("Team is full"); // change to custom exception
            }

            if (team.getUsers().contains(userId)) {
                throw new IllegalStateException("User already in team"); // change to custom exception
            }

            team.getUsers().add(userId);
            return teamRepository.save(team);
        } else if (key.startsWith("s")) {
            Course course = courseRepository.findByCourseKey(key)
                    .orElseThrow(() -> new CourseNotFoundException("Course not found with key: " + key)); // change to custom exception

            validateUserNotInCourse(userId, course.getId());

            Team newTeam = Team.builder()
                    .name(course.getName() + " - Solo Team")
                    .key("t" + UUID.randomUUID().toString().substring(0, 8))
                    .course(course)
                    .users(List.of(userId))
                    .active(true)
                    .build();

            return teamRepository.save(newTeam);
        }

        throw new IllegalArgumentException("Invalid key format"); // change to custom exception
    }

    @Override
    public Team removeUserFromTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found")); // change to custom exception

        team.getUsers().remove(userId);

        return teamRepository.save(team);
    }
}