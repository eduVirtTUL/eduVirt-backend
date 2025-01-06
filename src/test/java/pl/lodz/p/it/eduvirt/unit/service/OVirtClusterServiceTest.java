package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.Error;
import org.ovirt.engine.sdk4.services.*;
import org.ovirt.engine.sdk4.types.*;
import pl.lodz.p.it.eduvirt.exceptions.*;
import pl.lodz.p.it.eduvirt.service.impl.OVirtClusterServiceImpl;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OVirtClusterServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private OVirtClusterServiceImpl oVirtClusterService;

    /* Mock services */

    @Mock
    private SystemService systemService;

    @Mock
    private ClustersService clustersService;

    @Mock
    private HostsService hostsService;

    @Mock
    private VmsService vmsService;

    @Mock
    private EventsService eventsService;

    @Mock
    private ClusterService clusterService;

    /* Tests */

    /* FindClusterById method tests */

    @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindClusterById_Then_ReturnsFoundClusterSuccessfully() {
        UUID existingClusterId = UUID.randomUUID();

        ClusterService.GetRequest getRequest = mock(ClusterService.GetRequest.class);
        ClusterService.GetResponse getResponse = mock(ClusterService.GetResponse.class);
        Cluster cluster = mock(Cluster.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.clustersService()).thenReturn(clustersService);
        when(clustersService.clusterService(Mockito.eq(existingClusterId.toString()))).thenReturn(clusterService);
        when(clusterService.get()).thenReturn(getRequest);
        when(getRequest.send()).thenReturn(getResponse);
        when(getResponse.cluster()).thenReturn(cluster);

        Cluster foundCluster = oVirtClusterService.findClusterById(existingClusterId);

        assertNotNull(foundCluster);
        assertEquals(cluster, foundCluster);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).clustersService();
        verify(clustersService, times(1)).clusterService(Mockito.eq(existingClusterId.toString()));
        verify(clusterService, times(1)).get();
        verify(getRequest, times(1)).send();
        verify(getResponse, times(1)).cluster();
    }

    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindClusterById_Then_ThrowsException() {
        UUID nonExistentClusterId = UUID.randomUUID();

        ClusterService.GetRequest getRequest = mock(ClusterService.GetRequest.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.clustersService()).thenReturn(clustersService);
        when(clustersService.clusterService(Mockito.eq(nonExistentClusterId.toString()))).thenReturn(clusterService);
        when(clusterService.get()).thenReturn(getRequest);
        when(getRequest.send()).thenThrow(new org.ovirt.engine.sdk4.Error("Cluster not found"));

        assertThrows(ClusterNotFoundException.class,
                () -> oVirtClusterService.findClusterById(nonExistentClusterId));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).clustersService();
        verify(clustersService, times(1)).clusterService(Mockito.eq(nonExistentClusterId.toString()));
        verify(clusterService, times(1)).get();
        verify(getRequest, times(1)).send();
    }

    /* FindClusters method tests */

    @Test
    public void Given_SomeClustersExistInTheOVirtSystem_When_FindClusters_Then_ReturnsFoundClustersSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;

        String searchQuery = "page %s".formatted(pageNumber + 1);

        ClustersService.ListRequest listRequest = mock(ClustersService.ListRequest.class);
        ClustersService.ListResponse listResponse = mock(ClustersService.ListResponse.class);

        Cluster cluster1 = mock(Cluster.class);
        Cluster cluster2 = mock(Cluster.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.clustersService()).thenReturn(clustersService);
        when(clustersService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.clusters()).thenReturn(List.of(cluster1, cluster2));

        List<Cluster> foundClusters = oVirtClusterService.findClusters(pageNumber, pageSize);

        assertNotNull(foundClusters);
        assertFalse(foundClusters.isEmpty());
        assertEquals(2, foundClusters.size());

        Cluster firstCluster = foundClusters.getFirst();
        assertNotNull(firstCluster);
        assertEquals(cluster1, firstCluster);

        Cluster secondCluster = foundClusters.getLast();
        assertNotNull(secondCluster);
        assertEquals(cluster2, secondCluster);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).clustersService();
        verify(clustersService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).clusters();
    }

    @Test
    public void Given_NoClustersExistInTheOVirtSystem_When_FindClusters_Then_ReturnsEmptyClusterList() {
        int pageNumber = 0;
        int pageSize = 10;

        String searchQuery = "page %s".formatted(pageNumber + 1);

        ClustersService.ListRequest listRequest = mock(ClustersService.ListRequest.class);
        ClustersService.ListResponse listResponse = mock(ClustersService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.clustersService()).thenReturn(clustersService);
        when(clustersService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.clusters()).thenReturn(List.of());

        List<Cluster> foundClusters = oVirtClusterService.findClusters(pageNumber, pageSize);

        assertNotNull(foundClusters);
        assertTrue(foundClusters.isEmpty());

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).clustersService();
        verify(clustersService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).clusters();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtClass_When_FindClusters_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;

        String searchQuery = "page %s".formatted(pageNumber + 1);

        ClustersService.ListRequest listRequest = mock(ClustersService.ListRequest.class);
        ClustersService.ListResponse listResponse = mock(ClustersService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.clustersService()).thenReturn(clustersService);
        when(clustersService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(ClusterNotFoundException.class,
                () -> oVirtClusterService.findClusters(pageNumber, pageSize));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).clustersService();
        verify(clustersService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
    }

    /* FindHostsInCluster method tests */

    @Test
    public void Given_SomeHostsExistInTheGivenOVirtCluster_When_FindHostsInCluster_Then_ReturnsAllFoundHostsSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of(host1, host2));

        List<Host> foundHosts = oVirtClusterService.findHostsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundHosts);
        assertFalse(foundHosts.isEmpty());
        assertEquals(2, foundHosts.size());

        Host firstHost = foundHosts.getFirst();
        assertNotNull(firstHost);
        assertEquals(host1, firstHost);

        Host secondHost = foundHosts.getLast();
        assertNotNull(secondHost);
        assertEquals(host2, secondHost);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_NoHostsExistInTheGivenOVirtCluster_When_FindHostsInCluster_Then_ReturnsEmptyHostList() {
        int pageNumber = 0;
        int pageSize = 10;
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of());

        List<Host> foundHosts = oVirtClusterService.findHostsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundHosts);
        assertTrue(foundHosts.isEmpty());

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_FindHostsInCluster_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(HostNotFoundException.class,
                () -> oVirtClusterService.findHostsInCluster(cluster, pageNumber, pageSize));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
    }

    /* FindAllHostsInCluster method tests */

    @Test
    public void Given_SomeHostsExistInGivenOVirtCluster_When_FindAllHostsInCluster_Then_ReturnsAllHostsFoundForGivenCluster() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of(host1, host2));

        List<Host> foundHosts = oVirtClusterService.findAllHostsInCluster(cluster);

        assertNotNull(foundHosts);
        assertFalse(foundHosts.isEmpty());
        assertEquals(2, foundHosts.size());

        Host firstHost = foundHosts.getFirst();
        assertNotNull(firstHost);
        assertEquals(host1, firstHost);

        Host secondHost = foundHosts.getLast();
        assertNotNull(secondHost);
        assertEquals(host2, secondHost);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_NoHostsExistInGivenOVirtCluster_When_FindAllHostsInCluster_Then_ReturnsEmptyHostList() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of());

        List<Host> foundHosts = oVirtClusterService.findAllHostsInCluster(cluster);

        assertNotNull(foundHosts);
        assertTrue(foundHosts.isEmpty());

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindAllHostsInCluster_Then_ThrowsException() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(HostNotFoundException.class,
                () -> oVirtClusterService.findAllHostsInCluster(cluster));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
    }

    /* FindVmsInCluster method tests */

    @Test
    public void Given_SomeVmsExistInTheGivenOVirtCluster_When_FindVmsInCluster_Then_ReturnsAllFoundVmsSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        Vm vm1 = mock(Vm.class);
        Vm vm2 = mock(Vm.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.vms()).thenReturn(List.of(vm1, vm2));

        List<Vm> foundVms = oVirtClusterService.findVmsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundVms);
        assertFalse(foundVms.isEmpty());
        assertEquals(2, foundVms.size());

        Vm firstVm = foundVms.getFirst();
        assertNotNull(firstVm);
        assertEquals(vm1, firstVm);

        Vm secondVm = foundVms.getLast();
        assertNotNull(secondVm);
        assertEquals(vm2, secondVm);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).vms();
    }

    @Test
    public void Given_NoVmsExistInTheGivenOVirtCluster_When_FindVmsInCluster_Then_ReturnsEmptyVmList() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.vms()).thenReturn(List.of());

        List<Vm> foundVms = oVirtClusterService.findVmsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundVms);
        assertTrue(foundVms.isEmpty());

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).vms();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindVmsInCluster_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(VmNotFoundException.class,
                () -> oVirtClusterService.findVmsInCluster(cluster, pageNumber, pageSize));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
    }

    /* FindNetworksInCluster method tests */

    @Test
    public void Given_SomeNetworksExistInGivenOVirtCluster_When_FindNetworksInCluster_Then_ReturnsAllFoundNetworksSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);
        Network network1 = mock(Network.class);
        Network network2 = mock(Network.class);

        List<Network> allNetworks = List.of(network1, network2);
        when(cluster.networks()).thenReturn(allNetworks);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(allNetworks)).thenReturn(allNetworks);

        List<Network> foundNetworks = oVirtClusterService.findNetworksInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundNetworks);
        assertFalse(foundNetworks.isEmpty());
        assertEquals(2, foundNetworks.size());

        Network firstNetwork = foundNetworks.getFirst();
        assertNotNull(firstNetwork);
        assertEquals(network1, firstNetwork);

        Network secondNetwork = foundNetworks.getLast();
        assertNotNull(secondNetwork);
        assertEquals(network2, secondNetwork);

        verify(cluster, times(1)).networks();
        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(allNetworks));
    }

    @Test
    public void Given_NoNetworksExistInGivenOVirtCluster_When_FindNetworksInCluster_Then_ReturnsEmptyNetworksList() {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        List<Network> allNetworks = List.of();
        when(cluster.networks()).thenReturn(allNetworks);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(allNetworks)).thenReturn(allNetworks);

        List<Network> foundNetworks = oVirtClusterService.findNetworksInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundNetworks);
        assertTrue(foundNetworks.isEmpty());

        verify(cluster, times(1)).networks();
        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(allNetworks));
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindNetworksInCluster_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);
        Network network1 = mock(Network.class);
        Network network2 = mock(Network.class);

        List<Network> allNetworks = List.of(network1, network2);
        when(cluster.networks()).thenReturn(allNetworks);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(allNetworks)).thenThrow(Error.class);

        assertThrows(NetworkNotFoundException.class,
                () -> oVirtClusterService.findNetworksInCluster(cluster, pageNumber, pageSize));

        verify(cluster, times(1)).networks();
        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(allNetworks));
    }

    /* FindEventsInCluster method tests */

    @Test
    public void Given_SomeEventsExistForGivenOVirtCluster_When_FindEventsInCluster_Then_ReturnsAllFoundEventsSuccessfully() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);
        EventsService.ListResponse listResponse = mock(EventsService.ListResponse.class);

        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.events()).thenReturn(List.of(event1, event2));

        List<Event> foundEvents = oVirtClusterService.findEventsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundEvents);
        assertFalse(foundEvents.isEmpty());
        assertEquals(2, foundEvents.size());

        Event firstEvent = foundEvents.getFirst();
        assertNotNull(firstEvent);
        assertEquals(event1, firstEvent);

        Event secondEvent = foundEvents.getLast();
        assertNotNull(secondEvent);
        assertEquals(event2, secondEvent);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).eventsService();
        verify(eventsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).events();
    }

    @Test
    public void Given_NoEventsExistForGivenOVirtCluster_When_FindEventsInCluster_Then_ReturnsEmptyEventsList() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);
        EventsService.ListResponse listResponse = mock(EventsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.events()).thenReturn(List.of());

        List<Event> foundEvents = oVirtClusterService.findEventsInCluster(cluster, pageNumber, pageSize);

        assertNotNull(foundEvents);
        assertTrue(foundEvents.isEmpty());

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).eventsService();
        verify(eventsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).events();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindEventsInCluster_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;

        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s page %s".formatted(cluster.name(), pageNumber + 1);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);
        EventsService.ListResponse listResponse = mock(EventsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(EventNotFoundException.class,
                () -> oVirtClusterService.findEventsInCluster(cluster, pageNumber, pageSize));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).eventsService();
        verify(eventsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
    }


    /* FindHostCountInCluster method tests */

    @Test
    public void Given_SomeHostsExistInTheGivenOVirtCluster_When_FindHostCountInCluster_Then_ReturnsHostCountInTheGivenClusterSuccessfully() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        Host host3 = mock(Host.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of(host1, host2, host3));

        int hostCount = oVirtClusterService.findHostCountInCluster(cluster);

        assertNotEquals(0, hostCount);
        assertEquals(3, hostCount);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_NoHostsExistInTheGivenOVirtCluster_When_FindHostCountInCluster_Then_ReturnsHostCountInTheGivenClusterSuccessfully() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.hosts()).thenReturn(List.of());

        int hostCount = oVirtClusterService.findHostCountInCluster(cluster);

        assertEquals(0, hostCount);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).hosts();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindHostCountInCluster_Then_ThrowsException() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        HostsService.ListRequest listRequest = mock(HostsService.ListRequest.class);
        HostsService.ListResponse listResponse = mock(HostsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.hostsService()).thenReturn(hostsService);
        when(hostsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(HostNotFoundException.class,
                () -> oVirtClusterService.findHostCountInCluster(cluster));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).hostsService();
        verify(hostsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
    }

    /* FindVmCountInCluster method tests */

    @Test
    public void Given_SomeVmsExistInTheGivenOVirtCluster_When_FindVmCountInCluster_Then_ReturnsVmCountInTheGivenClusterSuccessfully() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        Vm vm1 = mock(Vm.class);
        Vm vm2 = mock(Vm.class);
        Vm vm3 = mock(Vm.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.vms()).thenReturn(List.of(vm1, vm2, vm3));

        int vmCount = oVirtClusterService.findVmCountInCluster(cluster);

        assertNotEquals(0, vmCount);
        assertEquals(3, vmCount);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).vms();
    }

    @Test
    public void Given_NoVmsExistInTheGivenOVirtCluster_When_FindVmCountInCluster_Then_ReturnsVmCountInTheGivenClusterSuccessfully() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.vms()).thenReturn(List.of());

        int vmCount = oVirtClusterService.findVmCountInCluster(cluster);

        assertEquals(0, vmCount);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
        verify(listResponse, times(1)).vms();
    }

    @Test
    public void Given_SomeExceptionIsThrownDuringOVirtCall_When_FindVmCountInCluster_Then_ThrowsException() {
        String exampleClusterName = "example_cluster_name";

        Cluster cluster = mock(Cluster.class);
        when(cluster.name()).thenReturn(exampleClusterName);

        String searchQuery = "cluster=%s".formatted(cluster.name());

        VmsService.ListRequest listRequest = mock(VmsService.ListRequest.class);
        VmsService.ListResponse listResponse = mock(VmsService.ListResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.list()).thenReturn(listRequest);
        when(listRequest.search(Mockito.eq(searchQuery))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(VmNotFoundException.class,
                () -> oVirtClusterService.findVmCountInCluster(cluster));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).send();
    }
}
