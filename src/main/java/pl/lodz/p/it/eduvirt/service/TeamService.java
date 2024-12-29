package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    List<Team> getAllTeams();
    Team getTeamById(UUID teamId);
    List<Team> getTeamsByUser(UUID userId);
    List<Team> getTeamsByCourse(UUID courseId);
    Team createTeam(Team team, UUID courseId, String keyValue);
    void createSoloTeam(UUID courseId, UUID userId);
    void addUserToTeam(String keyValue, UUID userId);
    void addUserToCourse(String keyValue, UUID userId);
    void removeUserFromTeam(UUID teamId, UUID userId);
    void joinUsingKey(String keyValue, UUID userId);
}
