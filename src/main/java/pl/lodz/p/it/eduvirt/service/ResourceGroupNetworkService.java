package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupNetworkService {
    ResourceGroupNetwork addResourceGroupNetwork(UUID rgId, String name);

    List<ResourceGroupNetwork> getResourceGroupNetworks(UUID rgId);

    void attachNicToNetwork(UUID networkId, UUID vmId, UUID nicId);

    void detachNicFromNetwork(UUID vmId, UUID nicId);

    void deleteNetwork(UUID networkId);
}
