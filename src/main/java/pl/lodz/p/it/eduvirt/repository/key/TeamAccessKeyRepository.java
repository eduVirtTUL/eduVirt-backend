package pl.lodz.p.it.eduvirt.repository.key;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;

import java.util.Optional;
import java.util.UUID;

public interface TeamAccessKeyRepository extends JpaRepository<TeamAccessKey, UUID> {
    Optional<TeamAccessKey> findByKeyValue(String keyValue);
    Optional<TeamAccessKey> findByTeamId(UUID teamId);
    boolean existsByTeamId(UUID teamId);
    boolean existsByKeyValue(String keyValue);

    @Modifying
    @Query("DELETE FROM TeamAccessKey k WHERE k.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") UUID teamId);
}