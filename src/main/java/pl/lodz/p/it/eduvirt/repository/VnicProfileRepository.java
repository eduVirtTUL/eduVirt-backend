package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface VnicProfileRepository extends JpaRepository<VnicProfilePoolMember, UUID> {

    @Query("SELECT v FROM VnicProfilePoolMember v WHERE v.id IN :ids")
    List<VnicProfilePoolMember> findAllWithIds(@Param("ids") Collection<UUID> ids);

    @Query("SELECT v FROM VnicProfilePoolMember v WHERE v.vlanId IN :vlanIds")
    List<VnicProfilePoolMember> findAllWithVlanIds(@Param("vlanIds") Collection<Integer> vlanIds,
                                                   Pageable pageable);

    @Query("SELECT v FROM VnicProfilePoolMember v WHERE v.vlanId IN :vlanIds AND v.inUse = false")
    List<VnicProfilePoolMember> findAllFreeVnicProfiles(@Param("vlanIds") Collection<Integer> vlanIds);
}
