package pl.lodz.p.it.eduvirt.service;

import java.util.UUID;

public interface VirtualMachineService {
    void createVirtualMachine(UUID rgId, UUID id, boolean hidden, String etag);

    void deleteVirtualMachine(UUID id, UUID rgId, String etag);

    void updateVirtualMachine(UUID id, boolean hidden);
}
