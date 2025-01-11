package pl.lodz.p.it.eduvirt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.lodz.p.it.eduvirt.entity.NetworkInterface;

import java.util.UUID;

@Repository
public interface NetworkInterfaceRepository extends JpaRepository<NetworkInterface, UUID> {
    void deleteByIdAndVirtualMachine_Id(UUID id, UUID vmId);
}
