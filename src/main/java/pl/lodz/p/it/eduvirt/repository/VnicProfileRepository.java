package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;

import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface VnicProfileRepository extends JpaRepository<VnicProfilePoolMember, UUID> {
}
