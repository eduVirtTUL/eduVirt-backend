package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import pl.lodz.p.it.eduvirt.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TeamService {

    /* Get methods */

    Team getTeamById(UUID teamId);

    Page<Team> getAllTeams(Pageable pageable);

    Page<Team> getTeamsByStudent(UUID userId, int page, int size, String search, String sortOrder);

    Page<Team> getTeamsByCourse(UUID courseId, int page, int size, String search, String searchType, String sortOrder);

    List<Team> findTeamsByEmails(UUID courseId, List<String> emailPrefixes, String sortOrder);

    Team getTeamByCourseAndUser(Course course, UUID userId);

    List<User> getStudentsInSoloCourse(Course course);

    /* Create, update, delete methods */

    Team createTeam(Team team, Course course, String keyValue);

    List<Team> createTeamsBatch(Course course, String prefix, int teamSize, int numberOfTeams);

    void createSoloTeam(Course course, User user);

    Team updateTeam(Team updatedTeam, UUID teamId, String etag);

    void deleteTeam(Team team);

    /* Join team or course methods */

    void joinUsingKey(String keyValue, User user);

    void addStudentToTeam(Team team, String email);

    void addStudentToCourse(Course course, String email);

    /* Leave team or course methods */

    void leaveTeam(UUID teamId, UUID userId);

    void removeStudentFromTeam(Team team, String email);

    void removeStudentFromCourse(Course course, String email);
}
