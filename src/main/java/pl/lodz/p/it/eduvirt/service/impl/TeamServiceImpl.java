package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.entity.key.CourseAccessKey;
import pl.lodz.p.it.eduvirt.entity.key.CourseType;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;
import pl.lodz.p.it.eduvirt.exceptions.access_key.AccessKeyNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.course.IncorrectCourseTypeException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.exceptions.user.*;
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
    private final PodStatefulRepository statefulPodRepository;
    private final PodStatelessRepository statelessPodRepository;

    private void validateUserNotInCourse(UUID userId, UUID courseId) {
        if (teamRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new UserAlreadyInCourseException();
        }
    }

    private void validateUserNotInTeam(Team team, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id %s could not be found!".formatted(userId)));

        if (team.getUsers().contains(user)) {
            throw new TeamUserAlreadyMemberException();
        }
    }

    private void validateTeamSizeAndName(Team team, UUID courseId) {
        if (teamRepository.existsByNameAndCourseId(team.getName(), courseId)) {
            throw new TeamAlreadyExistsException();
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team getTeamById(UUID teamId) {
        return teamRepository.findByIdWithUsers(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team getTeam(Team team) {
        return teamRepository.findByIdWithUsers(team.getId())
                .orElseThrow(() -> new TeamNotFoundException(team.getId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
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
                .orElseThrow(() -> new UserDoesntBelongToCourseException(userId, course.getId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team createTeam(Team team, UUID courseId, String userKeyValue) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        if (course.getCourseType() == CourseType.SOLO) {
            throw new IncorrectCourseTypeException("Cannot manually create a team in a solo course");
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
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (existingTeam.getCourse().getCourseType() == CourseType.SOLO) {
            existingTeam.setActive(updatedTeam.isActive());
            return teamRepository.saveAndFlush(existingTeam);
        }

        if (!existingTeam.getName().equals(updatedTeam.getName()) &&
                teamRepository.existsByNameAndCourseId(updatedTeam.getName(), existingTeam.getCourse().getId())) {
                throw new TeamAlreadyExistsException();
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

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id %s could not be found!".formatted(userId)));

        TeamAccessKey teamKey = teamKeyRepository.findByKeyValue(keyValue)
                .orElseThrow(AccessKeyNotFoundException::new);

        if (teamKey != null) {
            Team team = teamKey.getTeam();
            if (team.isActive()) {
                validateUserNotInTeam(team, userId);
                team.getUsers().add(user);
                teamRepository.saveAndFlush(team);
            } else {
                throw new TeamNotActiveException();
            }
        } else {
            CourseAccessKey courseKey = courseKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);
            Course course = courseKey.getCourse();

            if (course.getCourseType() == CourseType.TEAM_BASED) {
                throw new IncorrectCourseTypeException("Cannot join solo course with team-based access key");
            }

            validateUserNotInCourse(userId, course.getId());
            createSoloTeam(course, user);
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void leaveTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id %s could not be found!".formatted(userId)));

        if (team.getUsers().contains(user)) {
            team.getUsers().remove(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new TeamUserNotMemberException("User with id %s is not a member of team with id %s".formatted(userId, teamId));
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void addStudentToTeam(UUID teamId, String email) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(()-> new TeamNotFoundException(teamId));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User with email %s could not be found!".formatted(email)));

        if (team.isActive()) {
            validateUserNotInTeam(team, user.getId());
            team.getUsers().add(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new TeamNotActiveException();
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void addStudentToCourse(Course course, String email) {

        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Student with email %s could not be found!".formatted(email)));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Cannot add student directly to team-based course");
        }

        validateUserNotInCourse(student.getId(), course.getId());
        createSoloTeam(course, student);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void removeStudentFromCourse(Course course, String email) {

        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User with email %s could not be found!".formatted(email)));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Cannot remove student directly from team-based course");
        }

        Team team = teamRepository.findByUserIdAndCourse(student.getId(), course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team for user %s could not be found in course %s.".formatted(student.getId(), course.getId())));

        team.getUsers().remove(student);
        teamRepository.delete(team);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void removeStudentFromTeam(UUID teamId, String email) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User with email %s could not be found!".formatted(email)));

        if (team.getUsers().contains(user)) {
            team.getUsers().remove(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new TeamUserNotMemberException();
        }
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createSoloTeam(Course course, User user) {
        Long soloTeamCount = teamRepository.countByCourseId(course.getId());
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

    @Override
    @PreAuthorize("hasAnyAuthority('administrator', 'teacher')")
    public List<User> getStudentsInSoloCourse(Course course) {
        if (course.getCourseType() != CourseType.SOLO) {
            throw new IncorrectCourseTypeException("Can only get all students from solo courses");
        }

        List<Team> teams = teamRepository.findByCourseId(course.getId());
        return teams.stream()
                .flatMap(team -> team.getUsers().stream())
                .toList();
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteTeam(UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        if (team.getCourse().getCourseType() != CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Can only delete teams from team-based courses");
        }

        teamKeyRepository.deleteByTeamId(teamId);

        team.getStatefulPods().forEach(pod -> {
            pod.setTeam(null);
            pod.setCourse(null);
            statefulPodRepository.delete(pod);
        });

        team.getStatelessPods().forEach(pod -> {
            pod.setTeam(null);
            pod.setCourse(null);
            statelessPodRepository.delete(pod);
        });

        team.getStatefulPods().clear();
        team.getStatelessPods().clear();
        team.getUsers().clear();

        teamRepository.delete(team);
        teamRepository.flush();
    }
}