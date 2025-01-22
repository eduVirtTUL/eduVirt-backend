package pl.lodz.p.it.eduvirt.service.ovirt;

import org.ovirt.engine.sdk4.types.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OVirtClusterService {

    /* Read methods */

    Cluster findClusterById(UUID clusterId);

    List<Cluster> findClusters(Pageable pageable);

    List<Host> findHostsInCluster(Cluster cluster, Pageable pageable);

    List<Host> findAllHostsInCluster(Cluster cluster);

    List<Vm> findVmsInCluster(Cluster cluster, int pageNumber, int pageSize);

    List<Network> findNetworksInCluster(Cluster cluster, int pageNumber, int pageSize);

    List<Event> findEventsInCluster(Cluster cluster, Pageable pageable);

    int findHostCountInCluster(Cluster cluster);

    int findVmCountInCluster(Cluster cluster);
}
