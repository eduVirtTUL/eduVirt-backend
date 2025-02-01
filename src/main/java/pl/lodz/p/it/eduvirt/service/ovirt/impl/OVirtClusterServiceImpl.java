package pl.lodz.p.it.eduvirt.service.ovirt.impl;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.*;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtClusterService;
import pl.lodz.p.it.eduvirt.util.PaginationUtil;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class OVirtClusterServiceImpl implements OVirtClusterService {

    /* ConnectionFactory */

    private final ConnectionFactory connectionFactory;

    /* Read methods */

    @PreAuthorize("isAuthenticated()")
    @Override
    public Cluster findClusterById(UUID clusterId) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            ClusterService clusterService = systemService.clustersService().clusterService(clusterId.toString());
            return clusterService.get().send().cluster();
        } catch (Exception exception) {
            throw new ClusterNotFoundException(clusterId);
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public List<Cluster> findClusters(Pageable pageable) {
        try {
            SystemService systemService = connectionFactory.getConnection().systemService();
            ClustersService clustersService = systemService.clustersService();

            String sortBy = "";
            for (Sort.Order sortOrder : pageable.getSort()) {
                sortBy = "sortby %s %s ".formatted(sortOrder.getProperty(), sortOrder.getDirection());
                break;
            }

            String searchQuery = "%spage %s".formatted(sortBy, pageable.getPageNumber() + 1);
            return clustersService.list().search(searchQuery).max(pageable.getPageSize()).send().clusters();
        } catch (Exception exception) {
            throw new ClusterNotFoundException("No clusters could be found.");
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public List<Host> findHostsInCluster(Cluster cluster, Pageable pageable) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            HostsService hostsService = systemService.hostsService();

            String sortBy = "";
            for (Sort.Order sortOrder : pageable.getSort()) {
                sortBy = " sortby %s %s".formatted(sortOrder.getProperty(), sortOrder.getDirection());
                break;
            }

            String searchQuery = "cluster=%s%s page %s"
                    .formatted(cluster.name(), sortBy, pageable.getPageNumber() + 1);

            return hostsService.list().search(searchQuery).max(pageable.getPageSize()).send().hosts();
        } catch (Exception exception) {
            throw new HostNotFoundException(
                    "No hosts could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Host> findAllHostsInCluster(Cluster cluster) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            HostsService hostsService = systemService.hostsService();
            return hostsService.list().search("cluster=%s".formatted(cluster.name())).send().hosts();
        } catch (Exception exception) {
            throw new HostNotFoundException(
                    "No hosts could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Override
    public List<Vm> findVmsInCluster(Cluster cluster, int pageNumber, int pageSize) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();
            VmsService vmsService = systemService.vmsService();

            String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);
            return vmsService.list().search(searchQuery).max(pageSize).send().vms();
        } catch (Exception exception) {
            throw new VmNotFoundException(
                    "No vms could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public List<Network> findNetworksInCluster(Cluster cluster, int pageNumber, int pageSize) {
        try {
            Connection connection = connectionFactory.getConnection();

            return PaginationUtil.getPaginatedCollection(pageNumber, pageSize,
                    connection.followLink(cluster.networks())).stream().toList();
        } catch (Exception exception) {
            throw new NetworkNotFoundException(
                    "No networks could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public List<Event> findEventsInCluster(Cluster cluster, Pageable pageable) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            String sortBy = "";
            for (Sort.Order sortOrder : pageable.getSort()) {
                sortBy = " sortby %s %s".formatted(sortOrder.getProperty(), sortOrder.getDirection());
                break;
            }

            String args = "cluster=%s%s page %s".formatted(cluster.name(), sortBy, pageable.getPageNumber() + 1);
            return systemService.eventsService().list().search(args).max(pageable.getPageSize()).send().events();
        } catch (Exception exception) {
            throw new EventNotFoundException(
                    "No events could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public int findHostCountInCluster(Cluster cluster) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            String searchQuery = "cluster=%s".formatted(cluster.name());
            HostsService hostsService = systemService.hostsService();
            return hostsService.list().search(searchQuery).send().hosts().size();
        } catch (Exception exception) {
            throw new HostNotFoundException(
                    "No hosts could be found for cluster %s.".formatted(cluster.id()));
        }
    }

    @PreAuthorize("hasAuthority('administrator')")
    @Override
    public int findVmCountInCluster(Cluster cluster) {
        try {
            Connection connection = connectionFactory.getConnection();
            SystemService systemService = connection.systemService();

            String searchQuery = "cluster=%s".formatted(cluster.name());
            VmsService vmsService = systemService.vmsService();
            return vmsService.list().search(searchQuery).send().vms().size();
        } catch (Exception exception) {
            throw new VmNotFoundException(
                    "No vms could be found for cluster %s.".formatted(cluster.id()));
        }
    }
}
