package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroup;

import java.util.UUID;

@Repository
public interface ResourceGroupRepository extends JpaRepository<ResourceGroup, UUID> {
}
