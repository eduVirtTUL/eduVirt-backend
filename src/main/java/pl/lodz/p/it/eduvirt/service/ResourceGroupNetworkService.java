package pl.lodz.p.it.eduvirt.service;

import pl.lodz.p.it.eduvirt.entity.eduvirt.ResourceGroupNetwork;

import java.util.List;
import java.util.UUID;

public interface ResourceGroupNetworkService {
    ResourceGroupNetwork addResourceGroupNetwork(UUID rgId, String name);

    List<ResourceGroupNetwork> getResourceGroupNetworks(UUID rgId);

    void attachVmToNetwork(UUID networkId, UUID vmId);

    void detachVmFromNetwork(UUID networkId, UUID vmId);
}
