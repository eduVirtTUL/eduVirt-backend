package pl.lodz.p.it.eduvirt.service.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.types.*;
import org.ovirt.engine.sdk4.services.*;
import org.springframework.stereotype.Service;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;
import pl.lodz.p.it.eduvirt.util.PaginationUtil;

import java.util.List;
import java.util.UUID;

@Service
@LoggerInterceptor
@RequiredArgsConstructor
public class OVirtClusterServiceImpl implements OVirtClusterService {

    private final ConnectionFactory connectionFactory;

    @Override
    public Cluster findClusterById(UUID clusterId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            org.ovirt.engine.sdk4.services.ClusterService clusterService = systemService.clustersService().
                    clusterService(clusterId.toString());

            return clusterService.get().send().cluster();
        } catch (org.ovirt.engine.sdk4.Error error) {
            throw new ClusterNotFoundException();
        }
    }

    @Override
    public List<Cluster> findClusters(int pageNumber, int pageSize) {
        SystemService systemService = connectionFactory.getConnection().systemService();
        ClustersService clustersService = systemService.clustersService();

        String searchQuery = "page %s".formatted(pageNumber + 1);
        return clustersService.list().search(searchQuery).max(pageSize).send().clusters();
    }

    @Override
    public List<Host> findHostsInCluster(Cluster cluster, int pageNumber, int pageSize) {
        Connection connection = connectionFactory.getConnection();
        SystemService systemService = connection.systemService();

        HostsService hostsService = systemService.hostsService();

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
        return hostsService.list().search(searchQuery).max(pageSize).send().hosts();
    }

    @Override
    public List<Vm> findVmsInCluster(Cluster cluster, int pageNumber, int pageSize) {
        Connection connection = connectionFactory.getConnection();
        SystemService systemService = connection.systemService();

        VmsService vmsService = systemService.vmsService();

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
        return vmsService.list().search(searchQuery).max(pageSize).send().vms();
    }

    @Override
    public List<Network> findNetworksInCluster(Cluster cluster, int pageNumber, int pageSize) {
        Connection connection = connectionFactory.getConnection();

        return PaginationUtil.getPaginatedCollection(pageNumber, pageSize,
                connection.followLink(cluster.networks())).stream().toList();
    }

    @Override
    public List<Event> findEventsInCluster(Cluster cluster, int pageNumber, int pageSize) {
        Connection connection =  connectionFactory.getConnection();
        SystemService systemService = connection.systemService();

        String args = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
        return systemService.eventsService().list().search(args).max(pageSize).send().events();
    }

    @Override
    public int findHostCountInCluster(Cluster cluster) {
        Connection connection = connectionFactory.getConnection();
        SystemService systemService = connection.systemService();

        HostsService hostsService = systemService.hostsService();
        return hostsService.list().send().hosts().size();
    }

    @Override
    public int findVmCountInCluster(Cluster cluster) {
        Connection connection = connectionFactory.getConnection();
        SystemService systemService = connection.systemService();

        VmsService vmsService = systemService.vmsService();
        return vmsService.list().send().vms().size();
    }
}
