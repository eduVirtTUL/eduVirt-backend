package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import pl.lodz.p.it.eduvirt.exceptions.access_key.DuplicateKeyValueException;
import pl.lodz.p.it.eduvirt.exceptions.course.IncorrectCourseTypeException;
import pl.lodz.p.it.eduvirt.exceptions.team.*;
import pl.lodz.p.it.eduvirt.exceptions.user.*;
import pl.lodz.p.it.eduvirt.repository.*;
import pl.lodz.p.it.eduvirt.repository.key.CourseAccessKeyRepository;
import pl.lodz.p.it.eduvirt.repository.key.TeamAccessKeyRepository;
import pl.lodz.p.it.eduvirt.service.AccessKeyService;
import pl.lodz.p.it.eduvirt.service.KeyGeneratorService;
import pl.lodz.p.it.eduvirt.service.TeamService;
import pl.lodz.p.it.eduvirt.util.RoleConstants;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.REQUIRED)
public class TeamServiceImpl implements TeamService {

    /* Services */

    private final AccessKeyService accessKeyService;
    private final KeyGeneratorService keyGeneratorService;


    /* Repositories */

    private final TeamRepository teamRepository;
    private final TeamAccessKeyRepository teamKeyRepository;
    private final CourseAccessKeyRepository courseKeyRepository;
    private final UserRepository userRepository;

    /* Helper methods */

    private final ETagHelper eTagHelper;

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

    private void validateTeamName(Team team, UUID courseId) {
        if (teamRepository.existsByNameAndCourseId(team.getName(), courseId)) {
            throw new TeamAlreadyExistsException();
        }
    }

    private String generateTeamNamePrefix(String courseName) {
        return Arrays.stream(courseName.split("\\s+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.substring(0, 1).toUpperCase())
                .collect(Collectors.joining()) + "-Student";
    }

    private int findFirstAvailableNumber(List<Integer> existingNumbers) {
        if (existingNumbers.isEmpty()) {
            return 1;
        }

        int expected = 1;
        for (int actual : existingNumbers) {
            if (actual != expected) {
                return expected;
            }
            expected++;
        }
        return expected;
    }

    /* Service methods */

    /* Get methods */

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team getTeamById(UUID teamId) {
        return teamRepository.findByIdWithUsers(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));
    }

    @Override
    @PreAuthorize("hasAuthority('administrator')")
    public Page<Team> getAllTeams(Pageable pageable) {
        return teamRepository.findAllWithUsers(pageable);
    }

    @Override
    @PreAuthorize("hasAuthority('student')")
    public Page<Team> getTeamsByStudent(UUID userId, int page, int size, String search, String sortOrder) {
        Sort sort = null;
        if (Objects.equals(sortOrder, "ASC")) {
            sort = Sort.by("name").ascending();
        } else if ("DESC".equals(sortOrder)) {
            sort = Sort.by("name").descending();
        }

        if (search == null || search.isEmpty()) {
            return teamRepository.findByUsersId(userId, PageRequest.of(page, size, sort));
        }

        return teamRepository.findByUsersIdAndNameContainingIgnoreCase(userId, search, PageRequest.of(page, size, sort));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public Page<Team> getTeamsByCourse(UUID courseId, int page, int size, String search, String searchType, String sortOrder) {
        Sort sort = null;
        if (Objects.equals(sortOrder, "ASC")) {
            sort = Sort.by("name").ascending();
        } else if ("DESC".equals(sortOrder)) {
            sort = Sort.by("name").descending();
        }

        if (search == null || search.isEmpty()) {
            return teamRepository.findByCourseId(courseId, PageRequest.of(page, size, sort));
        }

        return teamRepository.findByCourseIdWithSearch(courseId, search, searchType, PageRequest.of(page, size, sort));
    }

    @Override
    @PreAuthorize("hasAnyAuthority('teacher', 'administrator')")
    public List<Team> findTeamsByEmails(UUID courseId, List<String> emailPrefixes, String sortOrder) {
        Sort sort = Sort.by(sortOrder.equals("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC, "name");
        return teamRepository.findByCourseIdAndEmailPrefixes(
                courseId,
                emailPrefixes.stream().map(String::toLowerCase).toList(),
                sort
        );
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Team getTeamByCourseAndUser(Course course, UUID userId) {
        return teamRepository.findByUserIdAndCourse(userId, course)
                .orElseThrow(() -> new UserDoesntBelongToCourseException(userId, course.getId()));
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

    /* Create, update, delete methods */

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    public Team createTeam(Team team, Course course, String userKeyValue) {
        if (userKeyValue != null && teamKeyRepository.existsByKeyValue(userKeyValue) && courseKeyRepository.existsByKeyValue(userKeyValue)) {
            throw new DuplicateKeyValueException(userKeyValue);
        }

        validateTeamName(team, course.getId());
        team.setCourse(course);
        team.setActive(true);
        team = teamRepository.saveAndFlush(team);

        String keyValue = userKeyValue != null ? userKeyValue : keyGeneratorService.generateUniqueTeamKey(course);
        accessKeyService.createTeamKey(team.getId(), keyValue);
        return team;
    }

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    public List<Team> createTeamsBatch(Course course, String prefix, int teamSize, int numberOfTeams) {
        if (course.getCourseType() != CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Can only create batch teams for team-based courses");
        }

        List<Integer> existingNumbers = teamRepository.findTeamNumbersByPrefix(course.getId(), prefix + "-");
        int startNumber = findFirstAvailableNumber(existingNumbers);
        List<Team> teams = new ArrayList<>();

        for (int i = 0; i < numberOfTeams; i++) {
            String teamName = prefix + "-" + (startNumber + i);
            Team team = Team.builder()
                    .name(teamName)
                    .course(course)
                    .maxSize(teamSize)
                    .active(true)
                    .users(new ArrayList<>())
                    .build();

            team = teamRepository.saveAndFlush(team);
            String keyValue = keyGeneratorService.generateUniqueTeamKey(course);
            accessKeyService.createTeamKey(team.getId(), keyValue);
            teams.add(team);
        }

        return teams;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void createSoloTeam(Course course, User user) {
        String namePrefix = generateTeamNamePrefix(course.getName());
        List<Integer> existingNumbers = teamRepository.findTeamNumbersByCourseIdAndPrefix(
                course.getId(),
                namePrefix
        );

        int teamNumber = findFirstAvailableNumber(existingNumbers);
        String teamName = namePrefix + teamNumber;

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
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public Team updateTeam(Team updatedTeam, UUID teamId, String etag) {
        Team existingTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (!eTagHelper.validateEtag(etag, existingTeam)) {
            throw new TeamConflictException();
        }

        if (existingTeam.getCourse().getCourseType() != CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Can only update teams in team-based courses");
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

        return teamRepository.saveAndFlush(existingTeam);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('teacher')")
    public void deleteTeam(Team team) {
        if (team.getCourse().getCourseType() != CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Can only delete teams manually from team-based courses");
        }

        Team finalTeam = team;
        team = teamRepository.findById(team.getId())
                .orElseThrow(() -> new TeamNotFoundException(finalTeam.getId()));

        if (!team.getStatefulPods().isEmpty() || !team.getStatelessPods().isEmpty()) {
            throw new TeamDeletionException("Team with id %s has associated pods - delete them before deleting the team.".formatted(team.getId()));
        }

        team.getUsers().clear();
        teamRepository.saveAndFlush(team);

        TeamAccessKey teamKey = teamKeyRepository.findByTeamId(team.getId())
                .orElse(null);
        if (teamKey != null) {
            teamKeyRepository.delete(teamKey);
            teamKeyRepository.flush();
        }

        teamRepository.delete(team);
        teamRepository.flush();
    }

    /* Join team or course methods */

    @Override
    @PreAuthorize("hasAuthority('student')")
    public void joinUsingKey(String keyValue, User user) {
        try {
            TeamAccessKey teamKey = teamKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);

            Team team = teamKey.getTeam();
            if (!team.isActive()) {
                throw new TeamNotActiveException();
            }

            validateUserNotInTeam(team, user.getId());
            team.getUsers().add(user);
            teamRepository.saveAndFlush(team);
        } catch (AccessKeyNotFoundException e) {
            CourseAccessKey courseKey = courseKeyRepository.findByKeyValue(keyValue)
                    .orElseThrow(AccessKeyNotFoundException::new);

            Course course = courseKey.getCourse();
            if (course.getCourseType() == CourseType.TEAM_BASED) {
                throw new IncorrectCourseTypeException("Cannot join solo course with team access key");
            }

            validateUserNotInCourse(user.getId(), course.getId());
            createSoloTeam(course, user);
        }
    }

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    public void addStudentToTeam(Team team, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Student with email %s could not be found!".formatted(email)));

        if (user.getRoles().contains(RoleConstants.STUDENT)) {
            if (team.getUsers().size() + 1 > team.getMaxSize()) {
                throw new TeamSizeException();
            }
            validateUserNotInTeam(team, user.getId());
            team.getUsers().add(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new UserNotAuthorizedException("User with email %s is not a student".formatted(email));
        }
    }

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    public void addStudentToCourse(Course course, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("Student with email %s could not be found!".formatted(email)));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Cannot add student directly to team-based course");
        }

        if (user.getRoles().contains(RoleConstants.STUDENT)) {
            validateUserNotInCourse(user.getId(), course.getId());
            createSoloTeam(course, user);
        } else {
            throw new UserNotAuthorizedException("User with email %s is not a student".formatted(email));
        }
    }

    /* Leave team or course methods */

    @Override
    @PreAuthorize("hasAuthority('student')")
    public void leaveTeam(UUID teamId, UUID userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

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
    @PreAuthorize("hasAuthority('teacher')")
    public void removeStudentFromTeam(Team team, String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User with email %s could not be found!".formatted(email)));

        if (team.getUsers().contains(user)) {
            team.getUsers().remove(user);
            teamRepository.saveAndFlush(team);
        } else {
            throw new TeamUserNotMemberException("User with email %s is not a member of team with id %s".formatted(email, team.getId()));
        }
    }

    @Override
    @PreAuthorize("hasAuthority('teacher')")
    public void removeStudentFromCourse(Course course, String email) {
        User student = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User with email %s could not be found!".formatted(email)));

        if (course.getCourseType() == CourseType.TEAM_BASED) {
            throw new IncorrectCourseTypeException("Cannot remove student with email %s manually from a team-based course".formatted(email));
        }

        Team team = teamRepository.findByUserIdAndCourse(student.getId(), course)
                .orElseThrow(() -> new TeamNotFoundException(
                        "Team for user %s could not be found in course %s.".formatted(student.getId(), course.getId())));

        team.getUsers().remove(student);
        teamRepository.delete(team);
    }
}