package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.key.AccessKey;
import pl.lodz.p.it.eduvirt.entity.key.AccessKeyType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessKeyRepository extends JpaRepository<AccessKey, UUID> {
    Optional<AccessKey> findByKeyValue(String keyValue);

    @Query("SELECT ak FROM AccessKey ak WHERE ak.course.id = :courseId AND ak.accessKeyType = :keyType")
    Optional<AccessKey> findByCourse(UUID courseId, AccessKeyType keyType);

    @Query("SELECT ak FROM AccessKey ak WHERE ak.team.id = :teamId")
    Optional<AccessKey> findByTeamId(UUID teamId);

    boolean existsByKeyValue(String keyValue);
    boolean existsByCourseIdAndAccessKeyType(UUID courseId, AccessKeyType keyType);
    boolean existsByTeamId(UUID teamId);
}