package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.Team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    Team createTeam(Team team, UUID courseId, String userKeyValue);
    List<Team> getAllTeams();
    Team getTeamById(UUID teamId);
    List<Team> getTeamsByUser(UUID userId);
    List<Team> getTeamsByCourse(UUID courseId);
    Team addUserToTeam(UUID teamId, UUID userId);
    void joinTeamOrCourse(String key, UUID userId);
    void removeUserFromTeam(UUID teamId, UUID userId);
}
