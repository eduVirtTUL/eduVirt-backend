package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TeamService {
    Page<Team> getAllTeams(Pageable pageable);

    Page<Team> getTeamsByUser(UUID userId, Pageable pageable);

    Page<Team> getTeamsByCourse(UUID courseId, Pageable pageable);

    Team getTeamById(UUID teamId);

    Team getTeamByCourseAndUser(Course course, UUID userId);

    Team createTeam(Team team, UUID courseId, String keyValue);

    Team updateTeam(Team team, UUID teamId);

    void createSoloTeam(UUID courseId, UUID userId);

    void joinUsingKey(String keyValue, UUID userId);

    void leaveTeam(UUID teamId, UUID userId);

    void addStudentToTeam(UUID teamId, String email);

    void addStudentToCourse(UUID courseId, String email);

    void removeStudentFromTeam(UUID teamId, String email);

    void removeStudentFromCourse(UUID courseId, String email);




}
