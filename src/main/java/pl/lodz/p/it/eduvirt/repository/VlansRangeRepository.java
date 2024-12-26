package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;

import java.util.UUID;

@Repository
public interface VlansRangeRepository extends JpaRepository<VlansRange, UUID> {

}
