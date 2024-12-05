package pl.lodz.p.it.eduvirt.service;

import jakarta.transaction.Transactional;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Team;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    Team createTeam(Team team, UUID courseId);

    List<Team> getTeams();

    Team getTeam(UUID teamId);

    List<Team> getTeamsByUser(UUID userId);

    List<Team> getTeamsByCourse(UUID courseId);

    Team addUserToTeam(UUID teamId, UUID userId);

    @Transactional
    Team joinTeamOrCreate(String key, UUID userId);

    Team removeUserFromTeam(UUID teamId, UUID userId);
}
