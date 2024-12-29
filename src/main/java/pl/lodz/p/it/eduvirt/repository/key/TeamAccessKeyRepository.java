package pl.lodz.p.it.eduvirt.repository.key;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.lodz.p.it.eduvirt.entity.key.TeamAccessKey;

import java.util.Optional;
import java.util.UUID;

public interface TeamAccessKeyRepository extends JpaRepository<TeamAccessKey, UUID> {
    Optional<TeamAccessKey> findByKeyValue(String keyValue);
    Optional<TeamAccessKey> findByTeamId(UUID teamId);
    boolean existsByTeamId(UUID teamId);
    boolean existsByKeyValue(String keyValue);
}