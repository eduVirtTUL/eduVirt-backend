package pl.lodz.p.it.eduvirt.repository.eduvirt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResourceGroupNetworkRepository extends JpaRepository<ResourceGroupNetwork, UUID> {
    List<ResourceGroupNetwork> getAllByResourceGroupId(UUID id);

    @Query("SELECT n FROM ResourceGroupNetwork n JOIN n.interfaces i WHERE i = :id")
    Optional<ResourceGroupNetwork> getNetworkByInterface(UUID id);

    Optional<ResourceGroupNetwork> findByResourceGroupIdAndInterfacesContains(UUID id, UUID nicId);
}
