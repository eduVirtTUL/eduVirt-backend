package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.*;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.exceptions.team.TeamNotFoundException;
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
@Transactional(propagation = Propagation.REQUIRED)
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final CourseRepository courseRepository;
    private final TeamAccessKeyRepository teamKeyRepository;
    private final CourseAccessKeyRepository courseKeyRepository;
    private final AccessKeyService accessKeyService;
    private final UserRepository userRepository;

    private void validateUserNotInCourse(UUID userId, UUID courseId) {
        if (teamRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new UserAlreadyInCourseException();
        }
    }

    private void validateUserNotInTeam(Team team, UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (team.getUsers().contains(user)) {
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
    public Team getTeamById(UUID teamId) {
        return teamRepository.findByIdWithUsers(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
    }

    @Override
    public Page<Team> getAllTeams(Pageable pageable) {
        return teamRepository.findAllWithUsers(pageable);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Page<Team> getTeamsByStudent(UUID userId, Pageable pageable) {
        return teamRepository.findByUsersId(userId, pageable);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Page<Team> getTeamsByCourse(UUID courseId, Pageable pageable) {
        return teamRepository.findByCourseId(courseId, pageable);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team getTeamByCourseAndUser(Course course, UUID userId) {
        return teamRepository.findByUserIdAndCourse(userId, course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team for user %s could not be found in course %s.".formatted(userId, course.getId())));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team createTeam(Team team, UUID courseId, String userKeyValue) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.SOLO) {
            throw new IncorrectTeamTypeException();
        }

        validateTeamSizeAndName(team, courseId);
        team.setCourse(course);
        team.setActive(true);
        team = teamRepository.saveAndFlush(team);

        accessKeyService.createTeamKey(team.getId(), userKeyValue);
        return team;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team updateTeam(Team updatedTeam, UUID teamId) {
        Team existingTeam = teamRepository.findById(teamId)
                .orElseThrow(RuntimeException::new);

        if (existingTeam.getCourse().getCourseType() == CourseType.SOLO) {
            existingTeam.setActive(updatedTeam.isActive());
            return teamRepository.saveAndFlush(existingTeam);
        }

        if (!existingTeam.getName().equals(updatedTeam.getName())) {
            if (teamRepository.existsByNameAndCourseId(updatedTeam.getName(), existingTeam.getCourse().getId())) {
                throw new TeamAlreadyExistsException();
            }
        }

        if (updatedTeam.getMaxSize() < 1 || updatedTeam.getMaxSize() > 8) {
            throw new TeamSizeException();
        }

        if (updatedTeam.getMaxSize() < existingTeam.getUsers().size()) {
            throw new TeamSizeException();
        }

        existingTeam.setName(updatedTeam.getName());
        existingTeam.setMaxSize(updatedTeam.getMaxSize());
        existingTeam.setActive(updatedTeam.isActive());

        return teamRepository.saveAndFlush(existingTeam);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void joinUsingKey(String keyValue, UUID userId) {

        if (keyValue == null || keyValue.isEmpty()) {
            throw new IllegalArgumentException("Key value cannot be empty");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        TeamAccessKey teamKey = teamKeyRepository.findByKeyValue(keyValue)
                .orElse(null);

        if (teamKey != null) {
            Team team = teamKey.getTeam();
            if (team.isActive()) {
                validateUserNotInTeam(team, userId);
                team.getUsers().add(user);
                teamRepository.saveAndFlush(team);
            } else {
                throw new RuntimeException("Team is not active");
            }
        }
        else {
            CourseAccessKey courseKey = courseKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);
            Course course = courseKey.getCourse();

            if (course.getCourseType() == CourseType.TEAM_BASED) {
                throw new IncorrectTeamTypeException(); //TODO: change to CourseTypeException
            }

            validateUserNotInCourse(userId, course.getId());
            createSoloTeam(course.getId(), userId);
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void leaveTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (team.getUsers().contains(user)) {
            team.getUsers().remove(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new RuntimeException("User is not in the team");
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void addStudentToTeam(UUID teamId, String email) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);

        teamKeyRepository.findByTeamId(teamId)
                .orElseThrow(AccessKeyNotFoundException::new);

        if (team.isActive()) {
            validateUserNotInTeam(team, user.getId());
            team.getUsers().add(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new RuntimeException("Team is not active");
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void addStudentToCourse(UUID courseId, String email) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectTeamTypeException(); //TODO: change to CourseTypeException
        }

        courseKeyRepository.findByCourseId(courseId)
                .orElseThrow(AccessKeyNotFoundException::new);

        validateUserNotInCourse(user.getId(), course.getId());
        createSoloTeam(course.getId(), user.getId());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void removeStudentFromTeam(UUID teamId, String email) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(RuntimeException::new);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);

        if (team.getUsers().contains(user)) {
            team.getUsers().remove(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new RuntimeException("User is not in the team");
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void removeStudentFromCourse(UUID courseId, String email) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectTeamTypeException(); //TODO: change to CourseTypeException
        }

        Team team = teamRepository.findByUserIdAndCourse(user.getId(), course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team for user %s could not be found in course %s.".formatted(user.getId(), course.getId())));

        team.getUsers().remove(user);
        teamRepository.saveAndFlush(team);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createSoloTeam(UUID courseId, UUID userId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (course.getCourseType() != CourseType.SOLO) {
            throw new IncorrectTeamTypeException();
        }

        Long soloTeamCount = teamRepository.countByCourseId(courseId);
        String teamName = course.getName() + " - Solo " + (soloTeamCount + 1);

        Team team = Team.builder()
                .name(teamName)
                .course(course)
                .users(List.of(user))
                .maxSize(1)
                .active(true)
                .build();

        teamRepository.saveAndFlush(team);
    }
}