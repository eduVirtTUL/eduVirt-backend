package pl.lodz.p.it.eduvirt.service;

import org.ovirt.engine.sdk4.types.*;

import java.util.List;
import java.util.UUID;

public interface IClusterService {

    Cluster findClusterById(UUID clusterId);
    List<Cluster> findClusters(int pageNumber, int pageSize);

    List<Host> findHostsInCluster(Cluster cluster, int pageNumber, int pageSize);
    List<Vm> findVmsInCluster(Cluster cluster, int pageNumber, int pageSize);
    List<Network> findNetworksInCluster(Cluster cluster, int pageNumber, int pageSize);
    List<Event> findEventsInCluster(Cluster cluster, int pageNumber, int pageSize);

    Integer findHostCountInCluster(Cluster cluster);
    Integer findVmCountInCluster(Cluster cluster);
}
