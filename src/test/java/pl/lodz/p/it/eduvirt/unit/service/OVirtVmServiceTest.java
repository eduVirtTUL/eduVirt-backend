package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.services.EventsService;
import org.ovirt.engine.sdk4.services.SystemService;
import org.ovirt.engine.sdk4.services.VmService;
import org.ovirt.engine.sdk4.services.VmsService;
import org.ovirt.engine.sdk4.types.*;
import pl.lodz.p.it.eduvirt.exceptions.EventNotFoundException;
import pl.lodz.p.it.eduvirt.service.impl.OVirtVmServiceImpl;
import pl.lodz.p.it.eduvirt.util.connection.ConnectionFactory;
import org.ovirt.engine.sdk4.Error;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OVirtVmServiceTest {

    @Mock
    private ConnectionFactory connectionFactory;

    @InjectMocks
    private OVirtVmServiceImpl oVirtVmService;

    @Mock
    private Connection connection;

    @Mock
    private SystemService systemService;

    @Mock
    private VmsService vmsService;

    @Mock
    private VmService vmService;

    @Mock
    private EventsService eventsService;

    /* Tests */

    /* FindStatisticsByVm method tests */

    @Test
    public void Given_SomeStatisticsAreDefinedForGivenVm_When_FindStatisticsByVm_Then_ReturnsAllFoundStatisticsSuccessfully() {
        Vm vm = mock(Vm.class);

        Statistic statistic1 = mock(Statistic.class);
        Statistic statistic2 = mock(Statistic.class);
        Statistic statistic3 = mock(Statistic.class);

        List<Statistic> listOfStatistics = List.of(statistic1, statistic2, statistic3);

        when(vm.statistics()).thenReturn(listOfStatistics);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(Mockito.eq(listOfStatistics))).thenReturn(listOfStatistics);

        List<Statistic> foundStatistics = oVirtVmService.findStatisticsByVm(vm);

        assertNotNull(foundStatistics);
        assertFalse(foundStatistics.isEmpty());
        assertEquals(3, foundStatistics.size());

        Statistic firstStatistic = foundStatistics.getFirst();
        assertNotNull(firstStatistic);
        assertEquals(statistic1, firstStatistic);

        Statistic secondStatistic = foundStatistics.get(1);
        assertNotNull(secondStatistic);
        assertEquals(statistic2, secondStatistic);

        Statistic thridStatistic = foundStatistics.getLast();
        assertNotNull(thridStatistic);
        assertEquals(statistic3, thridStatistic);

        verify(vm, times(1)).statistics();
        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(listOfStatistics));
    }

    @Test
    public void Given_NoStatisticsAreDefinedForGivenVm_When_FindStatisticsByVm_Then_ReturnsEmptyStatisticListSuccessfully() {
        Vm vm = mock(Vm.class);

        List<Statistic> listOfStatistics = List.of();

        when(vm.statistics()).thenReturn(listOfStatistics);
        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(Mockito.eq(listOfStatistics))).thenReturn(listOfStatistics);

        List<Statistic> foundStatistics = oVirtVmService.findStatisticsByVm(vm);

        assertNotNull(foundStatistics);
        assertTrue(foundStatistics.isEmpty());

        verify(vm, times(1)).statistics();
        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(listOfStatistics));
    }

    /* FindVmResources method tests */

    @Test
    public void Given_QosIsDefinedForVmCpuAndClusterUsesThreadsAsCpus_When_FindVmResources_Then_ReturnsNumberOfCpusAndMemorySizeFoundForGivenVm() {
        Vm vm = mock(Vm.class);
        Host host = mock(Host.class);
        Cluster cluster = mock(Cluster.class);

        CpuProfile cpuProfileMock = mock(CpuProfile.class);
        Qos qosMock = mock(Qos.class);
        Cpu cpuMock = mock(Cpu.class);
        CpuTopology cpuTopologyMock = mock(CpuTopology.class);

        when(vm.cpuProfile()).thenReturn(cpuProfileMock);
        when(vm.memory()).thenReturn(BigInteger.valueOf(1048576));
        when(cpuProfileMock.qos()).thenReturn(qosMock);
        when(qosMock.cpuLimit()).thenReturn(BigInteger.valueOf(40));

        when(host.cpu()).thenReturn(cpuMock);
        when(cpuMock.topology()).thenReturn(cpuTopologyMock);

        when(cpuTopologyMock.sockets()).thenReturn(BigInteger.valueOf(2));
        when(cpuTopologyMock.cores()).thenReturn(BigInteger.valueOf(4));
        when(cpuTopologyMock.threads()).thenReturn(BigInteger.valueOf(2));

        when(cluster.threadsAsCores()).thenReturn(true);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(Mockito.eq(cpuProfileMock))).thenReturn(cpuProfileMock);
        when(connection.followLink(Mockito.eq(qosMock))).thenReturn(qosMock);

        Map<String, Object> foundResources = oVirtVmService.findVmResources(vm, host, cluster);

        assertNotNull(foundResources);
        assertNotNull(foundResources.get("cpu"));
        assertEquals(foundResources.get("cpu"), 7);
        assertNotNull(foundResources.get("memory"));
        assertEquals(foundResources.get("memory"), 1048576L);

        verify(vm, times(1)).cpuProfile();
        verify(vm, times(1)).memory();
        verify(cpuProfileMock, times(2)).qos();
        verify(qosMock, times(1)).cpuLimit();

        verify(host, times(1)).cpu();
        verify(cpuMock, times(1)).topology();

        verify(cpuTopologyMock, times(1)).sockets();
        verify(cpuTopologyMock, times(1)).cores();
        verify(cpuTopologyMock, times(1)).threads();

        verify(cluster, times(1)).threadsAsCores();

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(cpuProfileMock));
        verify(connection, times(1)).followLink(Mockito.eq(qosMock));
    }

    @Test
    public void Given_QosIsDefinedForVmCpuAndClusterDoesNotUseThreadsAsCpus_When_FindVmResources_Then_ReturnsNumberOfCpusAndMemorySizeFoundForGivenVm() {
        Vm vm = mock(Vm.class);
        Host host = mock(Host.class);
        Cluster cluster = mock(Cluster.class);

        CpuProfile cpuProfileMock = mock(CpuProfile.class);
        Qos qosMock = mock(Qos.class);
        Cpu cpuMock = mock(Cpu.class);
        CpuTopology cpuTopologyMock = mock(CpuTopology.class);

        when(vm.cpuProfile()).thenReturn(cpuProfileMock);
        when(vm.memory()).thenReturn(BigInteger.valueOf(1048576));
        when(cpuProfileMock.qos()).thenReturn(qosMock);
        when(qosMock.cpuLimit()).thenReturn(BigInteger.valueOf(40));

        when(host.cpu()).thenReturn(cpuMock);
        when(cpuMock.topology()).thenReturn(cpuTopologyMock);

        when(cpuTopologyMock.sockets()).thenReturn(BigInteger.valueOf(2));
        when(cpuTopologyMock.cores()).thenReturn(BigInteger.valueOf(4));

        when(cluster.threadsAsCores()).thenReturn(false);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(Mockito.eq(cpuProfileMock))).thenReturn(cpuProfileMock);
        when(connection.followLink(Mockito.eq(qosMock))).thenReturn(qosMock);

        Map<String, Object> foundResources = oVirtVmService.findVmResources(vm, host, cluster);

        assertNotNull(foundResources);
        assertNotNull(foundResources.get("cpu"));
        assertEquals(foundResources.get("cpu"), 4);
        assertNotNull(foundResources.get("memory"));
        assertEquals(foundResources.get("memory"), 1048576L);

        verify(vm, times(1)).cpuProfile();
        verify(vm, times(1)).memory();
        verify(cpuProfileMock, times(2)).qos();
        verify(qosMock, times(1)).cpuLimit();

        verify(host, times(1)).cpu();
        verify(cpuMock, times(1)).topology();

        verify(cpuTopologyMock, times(1)).sockets();
        verify(cpuTopologyMock, times(1)).cores();

        verify(cluster, times(1)).threadsAsCores();

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(cpuProfileMock));
        verify(connection, times(1)).followLink(Mockito.eq(qosMock));
    }

    @Test
    public void Given_QosIsNotDefinedForVmCpu_When_FindVmResources_Then_ReturnsNumberOfCpusAndMemorySizeFoundForGivenVm() {
        Vm vm = mock(Vm.class);
        Host host = mock(Host.class);
        Cluster cluster = mock(Cluster.class);

        Cpu cpuMock = mock(Cpu.class);
        CpuProfile cpuProfileMock = mock(CpuProfile.class);
        CpuTopology cpuTopologyMock = mock(CpuTopology.class);

        when(vm.cpuProfile()).thenReturn(cpuProfileMock);
        when(vm.memory()).thenReturn(BigInteger.valueOf(1048576));
        when(vm.cpu()).thenReturn(cpuMock);
        when(cpuMock.topology()).thenReturn(cpuTopologyMock);

        when(cpuTopologyMock.sockets()).thenReturn(BigInteger.valueOf(2));
        when(cpuTopologyMock.cores()).thenReturn(BigInteger.valueOf(4));
        when(cpuTopologyMock.threads()).thenReturn(BigInteger.valueOf(2));

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.followLink(Mockito.eq(cpuProfileMock))).thenReturn(cpuProfileMock);

        Map<String, Object> foundResources = oVirtVmService.findVmResources(vm, host, cluster);

        assertNotNull(foundResources);
        assertNotNull(foundResources.get("cpu"));
        assertEquals(foundResources.get("cpu"), 16);
        assertNotNull(foundResources.get("memory"));
        assertEquals(foundResources.get("memory"), 1048576L);

        verify(vm, times(1)).cpu();
        verify(vm, times(1)).memory();
        verify(vm, times(1)).cpuProfile();

        verify(cpuMock, times(1)).topology();

        verify(cpuTopologyMock, times(1)).sockets();
        verify(cpuTopologyMock, times(1)).cores();
        verify(cpuTopologyMock, times(1)).threads();

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).followLink(Mockito.eq(cpuProfileMock));
    }

    /* FindVmById method tests */

    @Test
    public void Given_ExistingVmIdentifierIsPassed_When_FindVmById_Then_ReturnsFoundVmSuccessfully() {
        UUID vmId = UUID.randomUUID();

        Vm vm = mock(Vm.class);

        VmService.GetRequest getRequest = mock(VmService.GetRequest.class);
        VmService.GetResponse getResponse = mock(VmService.GetResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.vmService(Mockito.eq(vmId.toString()))).thenReturn(vmService);
        when(vmService.get()).thenReturn(getRequest);
        when(getRequest.follow(Mockito.eq("nics"))).thenReturn(getRequest);
        when(getRequest.send()).thenReturn(getResponse);
        when(getResponse.vm()).thenReturn(vm);

        Vm foundVm = oVirtVmService.findVmById(vmId.toString());

        assertNotNull(foundVm);
        assertEquals(vm, foundVm);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).vmService(Mockito.eq(vmId.toString()));
        verify(vmService, times(1)).get();
        verify(getRequest, times(1)).send();
        verify(getRequest, times(1)).follow(Mockito.eq("nics"));
        verify(getResponse, times(1)).vm();
    }

    @Test
    public void Given_NonExistentVmIdentifierIsPassed_When_FindVmById_Then_ThrowsException() {
        UUID vmId = UUID.randomUUID();
        VmService.GetRequest getRequest = mock(VmService.GetRequest.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.vmService(Mockito.eq(vmId.toString()))).thenReturn(vmService);
        when(vmService.get()).thenReturn(getRequest);
        when(getRequest.follow(Mockito.eq("nics"))).thenReturn(getRequest);
        when(getRequest.send()).thenThrow(new Error("Vm not found"));

        assertThrows(RuntimeException.class, () -> oVirtVmService.findVmById(vmId.toString()));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).vmService(Mockito.eq(vmId.toString()));
        verify(vmService, times(1)).get();
        verify(getRequest, times(1)).send();
    }

    /* FindNicsByVmId method tests */

    @Test
    public void Given_ExistingVmIdentifierIsPassedAndBelongsToSomeNetworks_When_FindNicsByVmId_Then_ReturnsListOfAllTheNetworksVmBelongsToSuccessfully() {
        UUID vmId = UUID.randomUUID();

        Vm vm = mock(Vm.class);

        Nic nic1 = mock(Nic.class);
        Nic nic2 = mock(Nic.class);
        Nic nic3 = mock(Nic.class);
        List<Nic> nicList = List.of(nic1, nic2, nic3);

        VmService.GetRequest getRequest = mock(VmService.GetRequest.class);
        VmService.GetResponse getResponse = mock(VmService.GetResponse.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.vmService(Mockito.eq(vmId.toString()))).thenReturn(vmService);
        when(vmService.get()).thenReturn(getRequest);
        when(getRequest.follow(Mockito.eq("nics"))).thenReturn(getRequest);
        when(getRequest.send()).thenReturn(getResponse);
        when(getResponse.vm()).thenReturn(vm);
        when(vm.nics()).thenReturn(nicList);

        List<Nic> foundNics = oVirtVmService.findNicsByVmId(vmId.toString());

        assertNotNull(foundNics);
        assertFalse(foundNics.isEmpty());
        assertEquals(3, foundNics.size());

        Nic firstNic = foundNics.getFirst();
        assertNotNull(firstNic);
        assertEquals(nic1, firstNic);

        Nic secondNic = foundNics.get(1);
        assertNotNull(secondNic);
        assertEquals(nic2, secondNic);

        Nic thridNic = foundNics.getLast();
        assertNotNull(thridNic);
        assertEquals(nic3, thridNic);

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).vmService(Mockito.eq(vmId.toString()));
        verify(vmService, times(1)).get();
        verify(getRequest, times(1)).send();
        verify(getRequest, times(1)).follow(Mockito.eq("nics"));
        verify(getResponse, times(1)).vm();
        verify(vm, times(1)).nics();
    }

    @Test
    public void Given_NonExistentVmIdentifierIsPassed_When_FindNicsByVmId_Then_ThrowsException() {
        UUID vmId = UUID.randomUUID();
        VmService.GetRequest getRequest = mock(VmService.GetRequest.class);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.vmsService()).thenReturn(vmsService);
        when(vmsService.vmService(Mockito.eq(vmId.toString()))).thenReturn(vmService);
        when(vmService.get()).thenReturn(getRequest);
        when(getRequest.follow(Mockito.eq("nics"))).thenReturn(getRequest);
        when(getRequest.send()).thenThrow(new Error("Vm not found"));

        assertThrows(RuntimeException.class, () -> oVirtVmService.findNicsByVmId(vmId.toString()));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).vmsService();
        verify(vmsService, times(1)).vmService(Mockito.eq(vmId.toString()));
        verify(vmService, times(1)).get();
        verify(getRequest, times(1)).follow(Mockito.eq("nics"));
        verify(getRequest, times(1)).send();
    }

    /* FindEventsByVmId method test */

    @Test
    public void Given_ExistentVmIdIsPassedAndSomeEventExistForGivenVm_When_FindEventsByVmId_Then_ReturnsFoundEvents() {
        int pageNumber = 0;
        int pageSize = 10;

        Vm vm = mock(Vm.class);
        String existingVmName = "EXAMPLE_VM_NAME";

        String searchQuery = "vm=%s page %s".formatted(existingVmName, pageNumber + 1);

        Event eventNo1 = mock(Event.class);
        Event eventNo2 = mock(Event.class);
        Event eventNo3 = mock(Event.class);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);
        EventsService.ListResponse listResponse = mock(EventsService.ListResponse.class);

        List<Event> listOfEvents = List.of(eventNo1, eventNo2, eventNo3);

        when(vm.name()).thenReturn(existingVmName);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(searchQuery)).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.events()).thenReturn(listOfEvents);

        List<Event> foundEvents = oVirtVmService.findEventsByVmId(vm, pageNumber, pageSize);

        assertNotNull(foundEvents);
        assertFalse(foundEvents.isEmpty());
        assertEquals(foundEvents.size(), 3);

        Event firstEvent = foundEvents.getFirst();
        assertNotNull(firstEvent);
        assertEquals(eventNo1, firstEvent);

        Event secondEvent = foundEvents.get(1);
        assertNotNull(secondEvent);
        assertEquals(eventNo2, secondEvent);

        Event thirdEvent = foundEvents.getLast();
        assertNotNull(thirdEvent);
        assertEquals(eventNo3, thirdEvent);

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
    public void Given_NonExistentVmIdIsPassed_When_FindEventsByVmId_Then_ThrowsException() {
        int pageNumber = 0;
        int pageSize = 10;

        Vm vm = mock(Vm.class);
        UUID existingVmId = UUID.randomUUID();
        String existingVmName = "EXAMPLE_VM_NAME";

        String searchQuery = "vm=%s page %s".formatted(existingVmName, pageNumber + 1);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);

        when(vm.id()).thenReturn(existingVmId.toString());
        when(vm.name()).thenReturn(existingVmName);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(searchQuery)).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenThrow(Error.class);

        assertThrows(EventNotFoundException.class,
                () -> oVirtVmService.findEventsByVmId(vm, pageNumber, pageSize));

        verify(connectionFactory, times(1)).getConnection();
        verify(connection, times(1)).systemService();
        verify(systemService, times(1)).eventsService();
        verify(eventsService, times(1)).list();
        verify(listRequest, times(1)).search(Mockito.eq(searchQuery));
        verify(listRequest, times(1)).max(Mockito.eq(pageSize));
        verify(listRequest, times(1)).send();
    }

    @Test
    public void Given_ExistentVmIdIsPassedAndNoEventExistForGivenVm_When_FindEventsByVmId_Then_ReturnsEmptyEventList() {
        int pageNumber = 0;
        int pageSize = 10;

        Vm vm = mock(Vm.class);
        String existingVmName = "EXAMPLE_VM_NAME";

        String searchQuery = "vm=%s page %s".formatted(existingVmName, pageNumber + 1);

        EventsService.ListRequest listRequest = mock(EventsService.ListRequest.class);
        EventsService.ListResponse listResponse = mock(EventsService.ListResponse.class);

        when(vm.name()).thenReturn(existingVmName);

        when(connectionFactory.getConnection()).thenReturn(connection);
        when(connection.systemService()).thenReturn(systemService);
        when(systemService.eventsService()).thenReturn(eventsService);
        when(eventsService.list()).thenReturn(listRequest);
        when(listRequest.search(searchQuery)).thenReturn(listRequest);
        when(listRequest.max(Mockito.eq(pageSize))).thenReturn(listRequest);
        when(listRequest.send()).thenReturn(listResponse);
        when(listResponse.events()).thenReturn(List.of());

        List<Event> foundEvents = oVirtVmService.findEventsByVmId(vm, pageNumber, pageSize);

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
}
