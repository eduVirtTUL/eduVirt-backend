package pl.lodz.p.it.eduvirt.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ovirt.engine.sdk4.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.OVirtAPIExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.ClusterController;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDTO;
import pl.lodz.p.it.eduvirt.dto.NetworkDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterDetailsDto;
import pl.lodz.p.it.eduvirt.dto.cluster.ClusterGeneralDto;
import pl.lodz.p.it.eduvirt.dto.host.HostDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmGeneralDto;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.*;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        ClusterController.class,
        GeneralControllerExceptionResolver.class,
        OVirtAPIExceptionResolver.class
})
@WebMvcTest(controllers = {ClusterController.class}, useDefaultFilters = false)
public class ClusterControllerTest {

    @MockitoBean
    private OVirtClusterService clusterService;

    @MockitoBean
    private OVirtVmService vmService;

    /* Mappers */

    @MockitoBean
    private ClusterMapper clusterMapper;

    @MockitoBean
    private HostMapper hostMapper;

    @MockitoBean
    private NetworkMapper networkMapper;

    @MockitoBean
    private EventMapper eventMapper;

    @MockitoBean
    private VmMapper vmMapper;

    /* Other */

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();
    private final UUID nonExistentClusterId = UUID.randomUUID();

    @BeforeEach
    public void prepareTestData() {
    }

    /* Tests */

    /* FindClusterById method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_FindClusterById_Then_ReturnsFoundClusterSuccessfully() throws Exception {
        String clusterName = "EXAMPLE_CLUSTER_NAME";
        String clusterComment = "EXAMPLE_CLUSTER_COMMENT";
        String clusterDescription = "EXAMPLE_CLUSTER_DESCRIPTION";
        String clusterCpuType = "CLUSTER_CPU_TYPE";
        int clusterMajor = 1;
        int clusterMinor = 2;
        boolean clusterUseThreadsAsCpus = true;
        String clusterMaxMemoryOverCommit = "100";

        Cluster cluster = mock(Cluster.class);

        ClusterDetailsDto clusterDetailsDto = new ClusterDetailsDto(
                existingClusterId.toString(),
                clusterName,
                clusterComment,
                clusterDescription,
                clusterCpuType,
                "%d.%d".formatted(clusterMajor, clusterMinor),
                clusterUseThreadsAsCpus,
                clusterMaxMemoryOverCommit
        );

        Cpu cpuMock = mock(Cpu.class);
        Version versionMock = mock(Version.class);
        MemoryPolicy memoryPolicyMock = mock(MemoryPolicy.class);
        MemoryOverCommit memoryOverCommitMock = mock(MemoryOverCommit.class);

        when(cluster.id()).thenReturn(existingClusterId.toString());
        when(cluster.name()).thenReturn(clusterName);
        when(cluster.comment()).thenReturn(clusterComment);
        when(cluster.description()).thenReturn(clusterDescription);
        when(cluster.cpu()).thenReturn(cpuMock);
        when(cpuMock.type()).thenReturn(clusterCpuType);
        when(cluster.version()).thenReturn(versionMock);
        when(versionMock.major()).thenReturn(BigInteger.ONE);
        when(versionMock.minor()).thenReturn(BigInteger.TWO);
        when(cluster.threadsAsCores()).thenReturn(clusterUseThreadsAsCpus);
        when(cluster.memoryPolicy()).thenReturn(memoryPolicyMock);
        when(memoryPolicyMock.overCommit()).thenReturn(memoryOverCommitMock);
        when(memoryOverCommitMock.percent()).thenReturn(BigInteger.valueOf(100));

        when(clusterService.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        when(clusterMapper.ovirtClusterToDetailsDto(cluster))
                .thenReturn(clusterDetailsDto);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}", existingClusterId))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ClusterDetailsDto foundCluster = mapper.readValue(json, ClusterDetailsDto.class);

        assertNotNull(foundCluster);
        assertNotNull(foundCluster.id());
        assertNotNull(foundCluster.name());
        assertNotNull(foundCluster.comment());
        assertNotNull(foundCluster.description());
        assertNotNull(foundCluster.clusterCpuType());
        assertNotNull(foundCluster.compatibilityVersion());
        assertNotNull(foundCluster.threadsAsCores());
        assertNotNull(foundCluster.maxMemoryOverCommit());

        assertEquals(clusterDetailsDto.id(), foundCluster.id());
        assertEquals(clusterDetailsDto.name(), foundCluster.name());
        assertEquals(clusterDetailsDto.comment(), foundCluster.comment());
        assertEquals(clusterDetailsDto.description(), foundCluster.description());
        assertEquals(clusterDetailsDto.clusterCpuType(), foundCluster.clusterCpuType());
        assertEquals(clusterDetailsDto.compatibilityVersion(), foundCluster.compatibilityVersion());
        assertEquals(clusterDetailsDto.threadsAsCores(), foundCluster.threadsAsCores());
        assertEquals(clusterDetailsDto.maxMemoryOverCommit(), foundCluster.maxMemoryOverCommit());

        verify(clusterService, times(1))
                .findClusterById(Mockito.eq(existingClusterId));

        verify(clusterMapper, times(1))
                .ovirtClusterToDetailsDto(Mockito.eq(cluster));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindClusterById_Then_ReturnsFoundClusterSuccessfully() throws Exception {
        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}", nonExistentClusterId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(clusterService, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterId));
    }

    /* FindAllClusters method tests */

    @WithMockUser
    @Test
    public void Given_SomeClustersExistInTheOVirtDB_When_FindAllClusters_Then_ReturnsAllFoundClusters() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        int hostCount1 = (int) (Math.random() * 99 + 1);
        int hostCount2 = (int) (Math.random() * 99 + 1);
        int hostCount3 = (int) (Math.random() * 99 + 1);

        int vmCount1 = (int) (Math.random() * 49 + 1);
        int vmCount2 = (int) (Math.random() * 49 + 1);
        int vmCount3 = (int) (Math.random() * 49 + 1);

        Cluster cluster1 = mock(Cluster.class);
        Cluster cluster2 = mock(Cluster.class);
        Cluster cluster3 = mock(Cluster.class);
        List<Cluster> clusterList = List.of(cluster1, cluster2, cluster3);

        ClusterGeneralDto clusterDto1 = new ClusterGeneralDto(
                UUID.randomUUID().toString(),
                "CLUSTER_NAME_1",
                "CLUSTER_DESCRIPTION_1",
                "CLUSTER_COMMENT_1",
                "CLUSTER_CPU_TYPE_1",
                "CLUSTER_COMPATIBILITY_VERSION_1",
                (long) hostCount1, (long) vmCount1
        );

        ClusterGeneralDto clusterDto2 = new ClusterGeneralDto(
                UUID.randomUUID().toString(),
                "CLUSTER_NAME_2",
                "CLUSTER_DESCRIPTION_2",
                "CLUSTER_COMMENT_2",
                "CLUSTER_CPU_TYPE_2",
                "CLUSTER_COMPATIBILITY_VERSION_2",
                (long) hostCount2, (long) vmCount2
        );

        ClusterGeneralDto clusterDto3 = new ClusterGeneralDto(
                UUID.randomUUID().toString(),
                "CLUSTER_NAME_3",
                "CLUSTER_DESCRIPTION_3",
                "CLUSTER_COMMENT_3",
                "CLUSTER_CPU_TYPE_3",
                "CLUSTER_COMPATIBILITY_VERSION_3",
                (long) hostCount3, (long) vmCount3
        );

        when(clusterService.findClusters(pageNumber, pageSize)).thenReturn(clusterList);
        when(clusterService.findHostCountInCluster(Mockito.any(Cluster.class))).thenReturn(hostCount1, hostCount2, hostCount3);
        when(clusterService.findVmCountInCluster(Mockito.any(Cluster.class))).thenReturn(vmCount1, vmCount2, vmCount3);

        when(clusterMapper.ovirtClusterToGeneralDto(Mockito.any(Cluster.class), Mockito.any(Long.class), Mockito.any(Long.class)))
                .thenReturn(clusterDto1, clusterDto2, clusterDto3);

        MvcResult result = mockMvc.perform(get("/clusters")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<ClusterGeneralDto> foundClusters = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(foundClusters);
        assertFalse(foundClusters.isEmpty());
        assertEquals(3, foundClusters.size());

        ClusterGeneralDto firstCluster = foundClusters.getFirst();
        assertNotNull(firstCluster);
        assertNotNull(firstCluster.id());
        assertNotNull(firstCluster.name());
        assertNotNull(firstCluster.description());
        assertNotNull(firstCluster.comment());
        assertNotNull(firstCluster.clusterCpuType());
        assertNotNull(firstCluster.compatibilityVersion());
        assertNotNull(firstCluster.hostCount());
        assertNotNull(firstCluster.vmCount());

        assertEquals(clusterDto1.id(), firstCluster.id());
        assertEquals(clusterDto1.name(), firstCluster.name());
        assertEquals(clusterDto1.description(), firstCluster.description());
        assertEquals(clusterDto1.comment(), firstCluster.comment());
        assertEquals(clusterDto1.clusterCpuType(), firstCluster.clusterCpuType());
        assertEquals(clusterDto1.compatibilityVersion(), firstCluster.compatibilityVersion());
        assertEquals(clusterDto1.hostCount(), firstCluster.hostCount());
        assertEquals(clusterDto1.vmCount(), firstCluster.vmCount());

        ClusterGeneralDto secondCluster = foundClusters.get(1);
        assertNotNull(secondCluster);
        assertNotNull(secondCluster.id());
        assertNotNull(secondCluster.name());
        assertNotNull(secondCluster.description());
        assertNotNull(secondCluster.comment());
        assertNotNull(secondCluster.clusterCpuType());
        assertNotNull(secondCluster.compatibilityVersion());
        assertNotNull(secondCluster.hostCount());
        assertNotNull(secondCluster.vmCount());

        assertEquals(clusterDto2.id(), secondCluster.id());
        assertEquals(clusterDto2.name(), secondCluster.name());
        assertEquals(clusterDto2.description(), secondCluster.description());
        assertEquals(clusterDto2.comment(), secondCluster.comment());
        assertEquals(clusterDto2.clusterCpuType(), secondCluster.clusterCpuType());
        assertEquals(clusterDto2.compatibilityVersion(), secondCluster.compatibilityVersion());
        assertEquals(clusterDto2.hostCount(), secondCluster.hostCount());
        assertEquals(clusterDto2.vmCount(), secondCluster.vmCount());

        ClusterGeneralDto thirdCluster = foundClusters.getLast();
        assertNotNull(thirdCluster);
        assertNotNull(thirdCluster.id());
        assertNotNull(thirdCluster.name());
        assertNotNull(thirdCluster.description());
        assertNotNull(thirdCluster.comment());
        assertNotNull(thirdCluster.clusterCpuType());
        assertNotNull(thirdCluster.compatibilityVersion());
        assertNotNull(thirdCluster.hostCount());
        assertNotNull(thirdCluster.vmCount());

        assertEquals(clusterDto3.id(), thirdCluster.id());
        assertEquals(clusterDto3.name(), thirdCluster.name());
        assertEquals(clusterDto3.description(), thirdCluster.description());
        assertEquals(clusterDto3.comment(), thirdCluster.comment());
        assertEquals(clusterDto3.clusterCpuType(), thirdCluster.clusterCpuType());
        assertEquals(clusterDto3.compatibilityVersion(), thirdCluster.compatibilityVersion());
        assertEquals(clusterDto3.hostCount(), thirdCluster.hostCount());
        assertEquals(clusterDto3.vmCount(), thirdCluster.vmCount());

        verify(clusterService, times(1)).findClusters(pageNumber, pageSize);
        verify(clusterService, times(3)).findHostCountInCluster(Mockito.any(Cluster.class));
        verify(clusterService, times(3)).findVmCountInCluster(Mockito.any(Cluster.class));

        verify(clusterMapper, times(3)).ovirtClusterToGeneralDto(
                Mockito.any(Cluster.class), Mockito.any(Long.class), Mockito.any(Long.class));
    }

    @WithMockUser
    @Test
    public void Given_NoClustersExistInTheOVirtDB_When_FindAllClusters_Then_ReturnsEmptyClusterList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        List<Cluster> clusterList = List.of();
        when(clusterService.findClusters(pageNumber, pageSize)).thenReturn(clusterList);

        mockMvc.perform(get("/clusters")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        verify(clusterService, times(1)).findClusters(pageNumber, pageSize);
    }

    /* FindClusterResourcesAvailability method tests */

    @WithMockUser
    @Test
    public void Given__When_FindClusterResourcesAvailability_Then_() throws Exception {
        // fail(); // TODO: Implement
    }

    /* FindHostInfoByClusterId method tests */

    @WithMockUser
    @Test
    public void Given_SomeHostsAreDefinedForTheGivenCluster_When_FindHostInfoByClusterId_Then_ReturnsAllFoundHostsForGivenCluster() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        Host host3 = mock(Host.class);
        List<Host> hostList = List.of(host1, host2, host3);

        int cpuCount1 = (int) (Math.random() * 99 + 1);
        int cpuCount2 = (int) (Math.random() * 99 + 1);
        int cpuCount3 = (int) (Math.random() * 99 + 1);

        int memorySize1 = (int) (Math.random() * 127 + 1) * 128 * 1024 * 1024;
        int memorySize2 = (int) (Math.random() * 127 + 1) * 128 * 1024 * 1024;
        int memorySize3 = (int) (Math.random() * 127 + 1) * 128 * 1024 * 1024;

        HostDto hostDto1 = new HostDto(
                UUID.randomUUID().toString(),
                "HOST_NAME_1",
                "HOST_ADDRESS_1",
                "HOST_COMMENT_1",
                (long) cpuCount1, (long) memorySize1
        );

        HostDto hostDto2 = new HostDto(
                UUID.randomUUID().toString(),
                "HOST_NAME_2",
                "HOST_ADDRESS_2",
                "HOST_COMMENT_2",
                (long) cpuCount2, (long) memorySize2
        );

        HostDto hostDto3 = new HostDto(
                UUID.randomUUID().toString(),
                "HOST_NAME_3",
                "HOST_ADDRESS_3",
                "HOST_COMMENT_3",
                (long) cpuCount3, (long) memorySize3
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findHostsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(hostList);
        when(hostMapper.ovirtHostToDto(Mockito.any(Host.class), Mockito.any(Cluster.class))).thenReturn(hostDto1, hostDto2, hostDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/hosts", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<HostDto> foundHosts = mapper.readValue(json, new TypeReference<>() {
        });

        assertNotNull(foundHosts);
        assertFalse(foundHosts.isEmpty());
        assertEquals(3, foundHosts.size());

        HostDto firstHost = foundHosts.getFirst();
        assertNotNull(firstHost);

        assertNotNull(firstHost.id());
        assertNotNull(firstHost.name());
        assertNotNull(firstHost.address());
        assertNotNull(firstHost.comment());
        assertNotNull(firstHost.cpus());
        assertNotNull(firstHost.memory());

        assertEquals(hostDto1.id(), firstHost.id());
        assertEquals(hostDto1.name(), firstHost.name());
        assertEquals(hostDto1.address(), firstHost.address());
        assertEquals(hostDto1.comment(), firstHost.comment());
        assertEquals(hostDto1.cpus(), firstHost.cpus());
        assertEquals(hostDto1.memory(), firstHost.memory());

        HostDto secondHost = foundHosts.get(1);
        assertNotNull(secondHost);

        assertNotNull(secondHost.id());
        assertNotNull(secondHost.name());
        assertNotNull(secondHost.address());
        assertNotNull(secondHost.comment());
        assertNotNull(secondHost.cpus());
        assertNotNull(secondHost.memory());

        assertEquals(hostDto2.id(), secondHost.id());
        assertEquals(hostDto2.name(), secondHost.name());
        assertEquals(hostDto2.address(), secondHost.address());
        assertEquals(hostDto2.comment(), secondHost.comment());
        assertEquals(hostDto2.cpus(), secondHost.cpus());
        assertEquals(hostDto2.memory(), secondHost.memory());

        HostDto thirdHost = foundHosts.getLast();
        assertNotNull(thirdHost);

        assertNotNull(thirdHost.id());
        assertNotNull(thirdHost.name());
        assertNotNull(thirdHost.address());
        assertNotNull(thirdHost.comment());
        assertNotNull(thirdHost.cpus());
        assertNotNull(thirdHost.memory());

        assertEquals(hostDto3.id(), thirdHost.id());
        assertEquals(hostDto3.name(), thirdHost.name());
        assertEquals(hostDto3.address(), thirdHost.address());
        assertEquals(hostDto3.comment(), thirdHost.comment());
        assertEquals(hostDto3.cpus(), thirdHost.cpus());
        assertEquals(hostDto3.memory(), thirdHost.memory());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findHostsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
        verify(hostMapper, times(3)).ovirtHostToDto(Mockito.any(Host.class), Mockito.any(Cluster.class));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindHostInfoByClusterId_Then_Returns400BadRequest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId))).thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/hosts", nonExistentClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NoHostsAreDefinedForTheGivenCluster_When_FindHostInfoByClusterId_Then_ReturnsEmptyHostList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        List<Host> hostList = List.of();
        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findHostsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(hostList);

        mockMvc.perform(get("/clusters/{clusterId}/hosts", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andReturn();

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findHostsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
    }

    /* FindVirtualMachinesByClusterId method tests */

    @WithMockUser
    @Test
    public void Given_SomeVmsAreDefinedForTheGivenCluster_When_FindVirtualMachinesByClusterId_Then_ReturnsAllFoundVmsForGivenCluster() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        Vm vm1 = mock(Vm.class);
        Vm vm2 = mock(Vm.class);
        Vm vm3 = mock(Vm.class);
        List<Vm> vmList = List.of(vm1, vm2, vm3);

        Statistic statistic1 = mock(Statistic.class);
        Statistic statistic2 = mock(Statistic.class);
        Statistic statistic3 = mock(Statistic.class);
        Statistic statistic4 = mock(Statistic.class);

        Value value1 = mock(Value.class);
        Value value2 = mock(Value.class);
        Value value3 = mock(Value.class);
        Value value4 = mock(Value.class);

        int elapsedTime1 = (int) (Math.random() * 11 + 1) * 3600;
        int elapsedTime2 = (int) (Math.random() * 11 + 1) * 3600;
        int elapsedTime3 = (int) (Math.random() * 11 + 1) * 3600;

        int cpuUsage1 = (int) (Math.random() * 100);
        int cpuUsage2 = (int) (Math.random() * 100);
        int cpuUsage3 = (int) (Math.random() * 100);

        int memoryUsage1 = (int) (Math.random() * 100);
        int memoryUsage2 = (int) (Math.random() * 100);
        int memoryUsage3 = (int) (Math.random() * 100);

        int networkUsage1 = (int) (Math.random() * 100);
        int networkUsage2 = (int) (Math.random() * 100);
        int networkUsage3 = (int) (Math.random() * 100);

        VmGeneralDto vmGeneralDto1 = new VmGeneralDto(
                UUID.randomUUID().toString(),
                "VM_NAME_1",
                "VM_STATUS_1",
                String.valueOf(elapsedTime1),
                cpuUsage1 + "%",
                memoryUsage1 + "%",
                networkUsage1  + "%"
        );

        VmGeneralDto vmGeneralDto2 = new VmGeneralDto(
                UUID.randomUUID().toString(),
                "VM_NAME_2",
                "VM_STATUS_2",
                String.valueOf(elapsedTime2),
                cpuUsage2 + "%",
                memoryUsage2 + "%",
                networkUsage2  + "%"
        );

        VmGeneralDto vmGeneralDto3 = new VmGeneralDto(
                UUID.randomUUID().toString(),
                "VM_NAME_3",
                "VM_STATUS_3",
                String.valueOf(elapsedTime3),
                cpuUsage3 + "%",
                memoryUsage3 + "%",
                networkUsage3  + "%"
        );

        when(statistic1.name()).thenReturn("elapsed.time");
        when(statistic2.name()).thenReturn("cpu.usage.history");
        when(statistic3.name()).thenReturn("memory.usage.history");
        when(statistic4.name()).thenReturn("network.usage.history");

        when(statistic1.values()).thenReturn(List.of(value1));
        when(statistic2.values()).thenReturn(List.of(value2));
        when(statistic3.values()).thenReturn(List.of(value3));
        when(statistic4.values()).thenReturn(List.of(value4));

        when(value1.datum()).thenReturn(BigDecimal.valueOf(elapsedTime1), BigDecimal.valueOf(elapsedTime2), BigDecimal.valueOf(elapsedTime3));
        when(value2.datum()).thenReturn(BigDecimal.valueOf(cpuUsage1), BigDecimal.valueOf(cpuUsage2), BigDecimal.valueOf(cpuUsage3));
        when(value3.datum()).thenReturn(BigDecimal.valueOf(memoryUsage1), BigDecimal.valueOf(memoryUsage2), BigDecimal.valueOf(memoryUsage3));
        when(value4.datum()).thenReturn(BigDecimal.valueOf(networkUsage1), BigDecimal.valueOf(networkUsage2), BigDecimal.valueOf(networkUsage3));

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findVmsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(vmList);

        when(vmService.findStatisticsByVm(Mockito.any(Vm.class)))
                .thenReturn(List.of(statistic1, statistic2, statistic3, statistic4));

        when(vmMapper.ovirtVmToGeneralDto(
                Mockito.any(Vm.class),
                Mockito.any(String.class),
                Mockito.any(String.class),
                Mockito.any(String.class),
                Mockito.any(String.class))
        ).thenReturn(vmGeneralDto1, vmGeneralDto2, vmGeneralDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/vms", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<VmGeneralDto> foundVms = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundVms);
        assertFalse(foundVms.isEmpty());
        assertEquals(3, foundVms.size());

        VmGeneralDto firstVm = foundVms.getFirst();
        assertNotNull(firstVm);

        assertNotNull(firstVm.id());
        assertNotNull(firstVm.name());
        assertNotNull(firstVm.status());
        assertNotNull(firstVm.uptimeSeconds());
        assertNotNull(firstVm.cpuUsagePercentage());
        assertNotNull(firstVm.memoryUsagePercentage());
        assertNotNull(firstVm.networkUsagePercentage());

        assertEquals(vmGeneralDto1.id(), firstVm.id());
        assertEquals(vmGeneralDto1.name(), firstVm.name());
        assertEquals(vmGeneralDto1.status(), firstVm.status());
        assertEquals(vmGeneralDto1.uptimeSeconds(), firstVm.uptimeSeconds());
        assertEquals(vmGeneralDto1.cpuUsagePercentage(), firstVm.cpuUsagePercentage());
        assertEquals(vmGeneralDto1.memoryUsagePercentage(), firstVm.memoryUsagePercentage());
        assertEquals(vmGeneralDto1.networkUsagePercentage(), firstVm.networkUsagePercentage());

        VmGeneralDto secondVm = foundVms.get(1);
        assertNotNull(secondVm);

        assertNotNull(secondVm.id());
        assertNotNull(secondVm.name());
        assertNotNull(secondVm.status());
        assertNotNull(secondVm.uptimeSeconds());
        assertNotNull(secondVm.cpuUsagePercentage());
        assertNotNull(secondVm.memoryUsagePercentage());
        assertNotNull(secondVm.networkUsagePercentage());

        assertEquals(vmGeneralDto2.id(), secondVm.id());
        assertEquals(vmGeneralDto2.name(), secondVm.name());
        assertEquals(vmGeneralDto2.status(), secondVm.status());
        assertEquals(vmGeneralDto2.uptimeSeconds(), secondVm.uptimeSeconds());
        assertEquals(vmGeneralDto2.cpuUsagePercentage(), secondVm.cpuUsagePercentage());
        assertEquals(vmGeneralDto2.memoryUsagePercentage(), secondVm.memoryUsagePercentage());
        assertEquals(vmGeneralDto2.networkUsagePercentage(), secondVm.networkUsagePercentage());

        VmGeneralDto thirdVm = foundVms.getLast();
        assertNotNull(thirdVm);

        assertNotNull(thirdVm.id());
        assertNotNull(thirdVm.name());
        assertNotNull(thirdVm.status());
        assertNotNull(thirdVm.uptimeSeconds());
        assertNotNull(thirdVm.cpuUsagePercentage());
        assertNotNull(thirdVm.memoryUsagePercentage());
        assertNotNull(thirdVm.networkUsagePercentage());

        assertEquals(vmGeneralDto3.id(), thirdVm.id());
        assertEquals(vmGeneralDto3.name(), thirdVm.name());
        assertEquals(vmGeneralDto3.status(), thirdVm.status());
        assertEquals(vmGeneralDto3.uptimeSeconds(), thirdVm.uptimeSeconds());
        assertEquals(vmGeneralDto3.cpuUsagePercentage(), thirdVm.cpuUsagePercentage());
        assertEquals(vmGeneralDto3.memoryUsagePercentage(), thirdVm.memoryUsagePercentage());
        assertEquals(vmGeneralDto3.networkUsagePercentage(), thirdVm.networkUsagePercentage());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findVmsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));

        verify(vmService, times(3)).findStatisticsByVm(Mockito.any(Vm.class));

        verify(vmMapper, times(3))
                .ovirtVmToGeneralDto(
                        Mockito.any(Vm.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class)
        );
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindVirtualMachinesByClusterId_Then_Returns400BadRequest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId))).thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/vms", nonExistentClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NoVmsAreDefinedForTheGivenCluster_When_FindVirtualMachinesByClusterId_Then_ReturnsEmptyVmList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        List<Vm> vmList = List.of();
        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findVmsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(vmList);

        mockMvc.perform(get("/clusters/{clusterId}/vms", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findVmsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
    }

    /* FindNetworksByClusterId method tests */

    @WithMockUser
    @Test
    public void Given_SomeNetworksAreDefinedForTheGivenCluster_When_FindNetworksByClusterId_Then_ReturnsAllFoundNetworksForGivenCluster() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        Network network1 = mock(Network.class);
        Network network2 = mock(Network.class);
        Network network3 = mock(Network.class);
        List<Network> networkList = List.of(network1, network2, network3);

        NetworkDto networkDto1 = new NetworkDto(
                UUID.randomUUID().toString(),
                "NETWORK_NAME_1",
                "NETWORK_DESCRIPTION_1",
                "NETWORK_COMMENT_1",
                "NETWORK_STATUS_1"
        );

        NetworkDto networkDto2 = new NetworkDto(
                UUID.randomUUID().toString(),
                "NETWORK_NAME_2",
                "NETWORK_DESCRIPTION_2",
                "NETWORK_COMMENT_2",
                "NETWORK_STATUS_2"
        );

        NetworkDto networkDto3 = new NetworkDto(
                UUID.randomUUID().toString(),
                "NETWORK_NAME_3",
                "NETWORK_DESCRIPTION_3",
                "NETWORK_COMMENT_3",
                "NETWORK_STATUS_3"
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findNetworksInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(networkList);
        when(networkMapper.ovirtNetworkToDto(Mockito.any(Network.class))).thenReturn(networkDto1, networkDto2, networkDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/networks", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<NetworkDto> foundNetworks = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundNetworks);
        assertFalse(foundNetworks.isEmpty());
        assertEquals(3, foundNetworks.size());

        NetworkDto firstNetwork = foundNetworks.getFirst();
        assertNotNull(firstNetwork);

        assertNotNull(firstNetwork.id());
        assertNotNull(firstNetwork.name());
        assertNotNull(firstNetwork.description());
        assertNotNull(firstNetwork.comment());
        assertNotNull(firstNetwork.status());

        assertEquals(networkDto1.id(), firstNetwork.id());
        assertEquals(networkDto1.name(), firstNetwork.name());
        assertEquals(networkDto1.status(), firstNetwork.status());
        assertEquals(networkDto1.comment(), firstNetwork.comment());
        assertEquals(networkDto1.description(), firstNetwork.description());

        NetworkDto secondNetwork = foundNetworks.get(1);
        assertNotNull(secondNetwork);

        assertNotNull(secondNetwork.id());
        assertNotNull(secondNetwork.name());
        assertNotNull(secondNetwork.description());
        assertNotNull(secondNetwork.comment());
        assertNotNull(secondNetwork.status());

        assertEquals(networkDto2.id(), secondNetwork.id());
        assertEquals(networkDto2.name(), secondNetwork.name());
        assertEquals(networkDto2.status(), secondNetwork.status());
        assertEquals(networkDto2.comment(), secondNetwork.comment());
        assertEquals(networkDto2.description(), secondNetwork.description());

        NetworkDto thirdNetwork = foundNetworks.getLast();
        assertNotNull(thirdNetwork);

        assertNotNull(thirdNetwork.id());
        assertNotNull(thirdNetwork.name());
        assertNotNull(thirdNetwork.description());
        assertNotNull(thirdNetwork.comment());
        assertNotNull(thirdNetwork.status());

        assertEquals(networkDto3.id(), thirdNetwork.id());
        assertEquals(networkDto3.name(), thirdNetwork.name());
        assertEquals(networkDto3.status(), thirdNetwork.status());
        assertEquals(networkDto3.comment(), thirdNetwork.comment());
        assertEquals(networkDto3.description(), thirdNetwork.description());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findNetworksInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
        verify(networkMapper, times(3)).ovirtNetworkToDto(Mockito.any(Network.class));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindNetworksByClusterId_Then_Returns400BadRequest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId))).thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/networks", nonExistentClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NoNetworksAreDefinedForTheGivenCluster_When_FindNetworksByClusterId_Then_ReturnsEmptyNetworkList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        List<Network> networkList = List.of();
        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findNetworksInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(networkList);

        mockMvc.perform(get("/clusters/{clusterId}/networks", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findNetworksInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
    }

    /* FindEventsByClusterId method tests */

    @WithMockUser
    @Test
    public void Given_SomeEventsAreDefinedForTheGivenCluster_When_FindEventsByClusterId_Then_ReturnsAllFoundEventsForGivenCluster() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        Event event3 = mock(Event.class);
        List<Event> eventList = List.of(event1, event2, event3);

        EventGeneralDTO eventDto1 = new EventGeneralDTO(
                UUID.randomUUID().toString(),
                "EVENT_MESSAGE_1",
                "EVENT_SEVERITY_1",
                "EVENT_REGISTERED_AT_1"
        );

        EventGeneralDTO eventDto2 = new EventGeneralDTO(
                UUID.randomUUID().toString(),
                "EVENT_MESSAGE_2",
                "EVENT_SEVERITY_2",
                "EVENT_REGISTERED_AT_2"
        );

        EventGeneralDTO eventDto3 = new EventGeneralDTO(
                UUID.randomUUID().toString(),
                "EVENT_MESSAGE_3",
                "EVENT_SEVERITY_3",
                "EVENT_REGISTERED_AT_3"
        );

        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findEventsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(eventList);
        when(eventMapper.ovirtEventToGeneralDTO(Mockito.any(Event.class))).thenReturn(eventDto1, eventDto2, eventDto3);

        MvcResult result = mockMvc.perform(get("/clusters/{clusterId}/events", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<EventGeneralDTO> foundEvents = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundEvents);
        assertFalse(foundEvents.isEmpty());
        assertEquals(3, foundEvents.size());

        EventGeneralDTO firstEvent = foundEvents.getFirst();
        assertNotNull(firstEvent);

        assertNotNull(firstEvent.id());
        assertNotNull(firstEvent.message());
        assertNotNull(firstEvent.severity());
        assertNotNull(firstEvent.registeredAt());

        assertEquals(eventDto1.id(), firstEvent.id());
        assertEquals(eventDto1.message(), firstEvent.message());
        assertEquals(eventDto1.severity(), firstEvent.severity());
        assertEquals(eventDto1.registeredAt(), firstEvent.registeredAt());

        EventGeneralDTO secondEvent = foundEvents.get(1);
        assertNotNull(secondEvent);

        assertNotNull(secondEvent.id());
        assertNotNull(secondEvent.message());
        assertNotNull(secondEvent.severity());
        assertNotNull(secondEvent.registeredAt());

        assertEquals(eventDto2.id(), secondEvent.id());
        assertEquals(eventDto2.message(), secondEvent.message());
        assertEquals(eventDto2.severity(), secondEvent.severity());
        assertEquals(eventDto2.registeredAt(), secondEvent.registeredAt());

        EventGeneralDTO thirdEvent = foundEvents.getLast();
        assertNotNull(thirdEvent);

        assertNotNull(thirdEvent.id());
        assertNotNull(thirdEvent.message());
        assertNotNull(thirdEvent.severity());
        assertNotNull(thirdEvent.registeredAt());

        assertEquals(eventDto3.id(), thirdEvent.id());
        assertEquals(eventDto3.message(), thirdEvent.message());
        assertEquals(eventDto3.severity(), thirdEvent.severity());
        assertEquals(eventDto3.registeredAt(), thirdEvent.registeredAt());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findEventsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
        verify(eventMapper, times(3)).ovirtEventToGeneralDTO(Mockito.any(Event.class));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindEventsByClusterId_Then_Returns400BadRequest() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId))).thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/clusters/{clusterId}/events", nonExistentClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_NoEventsAreDefinedForTheGivenCluster_When_FindEventsByClusterId_Then_ReturnsEmptyEventList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        Cluster cluster = mock(Cluster.class);

        List<Event> eventList = List.of();
        when(clusterService.findClusterById(Mockito.eq(existingClusterId))).thenReturn(cluster);
        when(clusterService.findEventsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize))).thenReturn(eventList);

        mockMvc.perform(get("/clusters/{clusterId}/events", existingClusterId)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(clusterService, times(1)).findEventsInCluster(Mockito.eq(cluster), Mockito.eq(pageNumber), Mockito.eq(pageSize));
    }
}
