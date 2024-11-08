package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePool;

import java.util.UUID;

@Repository
public interface VnicProfileRepository extends JpaRepository<VnicProfilePool, UUID> {
}
