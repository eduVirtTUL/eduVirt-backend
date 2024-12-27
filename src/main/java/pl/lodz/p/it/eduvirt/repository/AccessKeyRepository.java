package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.AccessKeyType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessKeyRepository extends JpaRepository<AccessKey, UUID> {
    Optional<AccessKey> findByKeyValue(String keyValue);
    boolean existsByKeyValue(String keyValue);
    boolean existsByCourseIdAndAccessKeyType(UUID courseId, AccessKeyType keyType);
    boolean existsByTeamId(UUID teamId);
}