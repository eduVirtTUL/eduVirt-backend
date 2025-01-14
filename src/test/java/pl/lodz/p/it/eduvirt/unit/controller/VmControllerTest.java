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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.VmController;
import pl.lodz.p.it.eduvirt.dto.EventGeneralDto;
import pl.lodz.p.it.eduvirt.dto.resources.ResourcesDto;
import pl.lodz.p.it.eduvirt.dto.vm.VmDto;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Updatable;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VmNotFoundException;
import pl.lodz.p.it.eduvirt.mappers.EventMapper;
import pl.lodz.p.it.eduvirt.mappers.EventMapperImpl;
import pl.lodz.p.it.eduvirt.mappers.VmMapper;
import pl.lodz.p.it.eduvirt.mappers.VmMapperImpl;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;
import pl.lodz.p.it.eduvirt.service.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.OVirtVnicProfileService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        VmController.class, GeneralControllerExceptionResolver.class,
        VmMapperImpl.class, EventMapperImpl.class
})
@WebMvcTest(controllers = {VmController.class}, useDefaultFilters = false)
public class VmControllerTest {

    /* MockMVC */

    @Autowired
    private MockMvc mockMvc;

    /* Services */

    @MockitoBean
    private OVirtVmService oVirtVmService;

    @MockitoBean
    private OVirtClusterService oVirtClusterService;

    @MockitoBean
    private OVirtVnicProfileService oVirtVnicProfileService;

    /* Mappers */

    @MockitoSpyBean
    private VmMapper vmMapper;

    @MockitoSpyBean
    private EventMapper eventMapper;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Data initialization */

    private final UUID existingVmIdentifier = UUID.randomUUID();
    private final UUID nonExistentVmIdentifier = UUID.randomUUID();

    private final UUID existingClusterIdentifier = UUID.randomUUID();
    private final UUID nonExistentClusterIdentifier = UUID.randomUUID();

    @BeforeEach
    public void setUp() throws Exception {
        mapper.findAndRegisterModules();

        Field id = AbstractEntity.class.getDeclaredField("id");
        Field version = Updatable.class.getDeclaredField("version");
    }

    /* Test methods */

    /* FindVmRequiredResources method tests */

    @Test
    @WithMockUser
    public void Given_ExistingVmIdentifierIsPassedForVmWithQosOnItsCpu_When_FindVmRequiredResources_Then_ReturnsResourcesRequiredByFoundVm() throws Exception {
        int cpuCount = 16;
        long memorySize = 10L * 1024 * 1024 * 1024;

        Vm vm = mock(Vm.class);
        CpuProfile cpuProfile = mock(CpuProfile.class);
        Qos qos = mock(Qos.class);
        Cluster cluster = mock(Cluster.class);

        Host host1 = mock(Host.class);
        Host host2 = mock(Host.class);
        Host host3 = mock(Host.class);
        List<Host> hosts = List.of(host1, host2, host3);

        Map<String, Object> resourcesMap = new HashMap<>();
        resourcesMap.put("cpu", cpuCount);
        resourcesMap.put("memory", memorySize);

        when(vm.cpuProfile()).thenReturn(cpuProfile);
        when(cpuProfile.qos()).thenReturn(qos);
        when(vm.cluster()).thenReturn(cluster);
        when(cluster.id()).thenReturn(existingClusterIdentifier.toString());

        when(oVirtVmService.findVmWithCpuProfileById(Mockito.eq(existingVmIdentifier.toString()))).thenReturn(vm);
        when(oVirtVmService.findQosForVmCpu(Mockito.eq(vm))).thenReturn(qos);
        when(oVirtClusterService.findClusterById(Mockito.eq(existingClusterIdentifier))).thenReturn(cluster);
        when(oVirtClusterService.findAllHostsInCluster(Mockito.eq(cluster))).thenReturn(hosts);
        when(oVirtVmService.findVmResources(Mockito.eq(vm), Mockito.eq(qos), Mockito.eq(host1), Mockito.eq(cluster)))
                .thenReturn(resourcesMap);

        MvcResult result = mockMvc.perform(get("/resource/vm/{vmId}/required-resources", existingVmIdentifier))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ResourcesDto outputDto = mapper.readValue(json, ResourcesDto.class);

        assertNotNull(outputDto);
        assertEquals(cpuCount, outputDto.cpuCount());
        assertEquals(memorySize, outputDto.memorySize());

        verify(oVirtVmService, times(1)).findVmWithCpuProfileById(Mockito.eq(existingVmIdentifier.toString()));
        verify(oVirtVmService, times(1)).findQosForVmCpu(Mockito.eq(vm));
        verify(oVirtClusterService, times(1)).findClusterById(Mockito.eq(existingClusterIdentifier));
        verify(oVirtClusterService, times(1)).findAllHostsInCluster(Mockito.eq(cluster));
        verify(oVirtVmService, times(1))
                .findVmResources(Mockito.eq(vm), Mockito.eq(qos), Mockito.eq(host1), Mockito.eq(cluster));
    }

    @Test
    @WithMockUser
    public void Given_ExistingVmIdentifierIsPassedForVmWithoutQosOnItsCpu_When_FindVmRequiredResources_Then_ReturnsResourcesRequiredByFoundVm() throws Exception {
        int cpuCount = 16;
        long memorySize = 10L * 1024 * 1024 * 1024;

        Vm vm = mock(Vm.class);
        CpuProfile cpuProfile = mock(CpuProfile.class);
        Qos qos = mock(Qos.class);

        Map<String, Object> resourcesMap = new HashMap<>();
        resourcesMap.put("cpu", cpuCount);
        resourcesMap.put("memory", memorySize);

        when(vm.cpuProfile()).thenReturn(cpuProfile);
        when(cpuProfile.qos()).thenReturn(null);

        when(oVirtVmService.findVmWithCpuProfileById(Mockito.eq(existingVmIdentifier.toString()))).thenReturn(vm);
        when(oVirtVmService.findVmResources(Mockito.eq(vm), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null)))
                .thenReturn(resourcesMap);

        MvcResult result = mockMvc.perform(get("/resource/vm/{vmId}/required-resources", existingVmIdentifier))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        ResourcesDto outputDto = mapper.readValue(json, ResourcesDto.class);

        assertNotNull(outputDto);
        assertEquals(cpuCount, outputDto.cpuCount());
        assertEquals(memorySize, outputDto.memorySize());

        verify(oVirtVmService, times(1)).findVmWithCpuProfileById(Mockito.eq(existingVmIdentifier.toString()));
        verify(oVirtVmService, times(1))
                .findVmResources(Mockito.eq(vm), Mockito.eq(null), Mockito.eq(null), Mockito.eq(null));
    }

    @Test
    @WithMockUser
    public void Given_NonExistentVmIdentifierIsPassed_When_FindVmRequiredResources_Then_Returns404NotFound() throws Exception {
        when(oVirtVmService.findVmWithCpuProfileById(Mockito.eq(nonExistentClusterIdentifier.toString())))
                .thenThrow(VmNotFoundException.class);

        mockMvc.perform(get("/resource/vm/{vmId}/required-resources", nonExistentClusterIdentifier))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtVmService, times(1)).findVmWithCpuProfileById(Mockito.eq(nonExistentClusterIdentifier.toString()));
    }

    /* FindVmsForCluster method tests */

    @Test
    @WithMockUser
    public void Given_ExistingClusterIdentifierIsPassedAndSomeVmsExistForGivenCluster_When_FindVmsForCluster_Then_ReturnsListOfFoundVmsForGivenCluster() throws Exception {
        Vm vm1 = mock(Vm.class);
        Vm vm2 = mock(Vm.class);
        Vm vm3 = mock(Vm.class);
        Cluster cluster = mock(Cluster.class);
        List<Vm> vms = List.of(vm1, vm2, vm3);

        when(vm1.id()).thenReturn(UUID.randomUUID().toString());
        when(vm2.id()).thenReturn(UUID.randomUUID().toString());
        when(vm3.id()).thenReturn(UUID.randomUUID().toString());

        when(vm1.name()).thenReturn("VM-1");
        when(vm2.name()).thenReturn("VM-2");
        when(vm3.name()).thenReturn("VM-3");

        when(oVirtClusterService.findClusterById(Mockito.eq(existingClusterIdentifier))).thenReturn(cluster);
        when(oVirtVmService.findVmsForCluster(Mockito.eq(cluster))).thenReturn(vms);

        MvcResult result = mockMvc.perform(get("/resource/vm/clusters/{clusterId}", existingClusterIdentifier))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<VmDto> foundVms = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundVms);
        assertFalse(foundVms.isEmpty());
        assertEquals(3, foundVms.size());

        VmDto firstVm = foundVms.getFirst();
        assertNotNull(firstVm);
        assertEquals(vm1.id(), firstVm.getId());
        assertEquals(vm1.name(), firstVm.getName());

        VmDto secondVm = foundVms.get(1);
        assertNotNull(secondVm);
        assertEquals(vm2.id(), secondVm.getId());
        assertEquals(vm2.name(), secondVm.getName());

        VmDto thirdVm = foundVms.getLast();
        assertNotNull(thirdVm);
        assertEquals(vm3.id(), thirdVm.getId());
        assertEquals(vm3.name(), thirdVm.getName());

        verify(oVirtClusterService, times(1)).findClusterById(Mockito.eq(existingClusterIdentifier));
        verify(oVirtVmService, times(1)).findVmsForCluster(Mockito.eq(cluster));
    }

    @Test
    @WithMockUser
    public void Given_ExistingClusterIdentifierIsPassedAndNoVmsAreFoundForGivenCluster_When_FindVmsForCluster_Then_ReturnsEmptyListOfVms() throws Exception {
        Cluster cluster = mock(Cluster.class);

        when(oVirtClusterService.findClusterById(Mockito.eq(existingClusterIdentifier))).thenReturn(cluster);
        when(oVirtVmService.findVmsForCluster(Mockito.eq(cluster))).thenReturn(List.of());

        mockMvc.perform(get("/resource/vm/clusters/{clusterId}", existingClusterIdentifier))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtClusterService, times(1)).findClusterById(Mockito.eq(existingClusterIdentifier));
        verify(oVirtVmService, times(1)).findVmsForCluster(Mockito.eq(cluster));
    }

    @Test
    @WithMockUser
    public void Given_NonExistentClusterIdentifierIsPassed_When_FindVmsForCluster_Then_Returns404NotFound() throws Exception {
        when(oVirtClusterService.findClusterById(Mockito.eq(nonExistentClusterIdentifier)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(get("/resource/vm/clusters/{clusterId}", nonExistentClusterIdentifier))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtClusterService, times(1))
                .findClusterById(Mockito.eq(nonExistentClusterIdentifier));
    }

    /* FindEventsForVm methods tests */

    @Test
    @WithMockUser
    public void Given_ExistingVmIdentifierIsPassedAndSomeEventsWereForVm_When_FindEventsForVm_Then_ReturnsListOfFoundEvents() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        LocalDateTime currentTime = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();

        Vm vm = mock(Vm.class);
        Event event1 = mock(Event.class);
        Event event2 = mock(Event.class);
        Event event3 = mock(Event.class);
        List<Event> events = List.of(event1, event2, event3);

        when(event1.id()).thenReturn(UUID.randomUUID().toString());
        when(event2.id()).thenReturn(UUID.randomUUID().toString());
        when(event3.id()).thenReturn(UUID.randomUUID().toString());

        when(event1.description()).thenReturn("Event1 description!");
        when(event2.description()).thenReturn("Event2 description!");
        when(event3.description()).thenReturn("Event3 description!");

        when(event1.severity()).thenReturn(LogSeverity.NORMAL);
        when(event2.severity()).thenReturn(LogSeverity.WARNING);
        when(event3.severity()).thenReturn(LogSeverity.ERROR);

        when(event1.time()).thenReturn(Date.from(currentTime.plusHours(2).toInstant(ZoneOffset.UTC)));
        when(event2.time()).thenReturn(Date.from(currentTime.plusHours(4).toInstant(ZoneOffset.UTC)));
        when(event3.time()).thenReturn(Date.from(currentTime.plusHours(6).toInstant(ZoneOffset.UTC)));

        when(oVirtVmService.findVmById(Mockito.eq(existingVmIdentifier.toString()))).thenReturn(vm);
        when(oVirtVmService.findEventsByVmId(Mockito.eq(vm), Mockito.eq(pageable))).thenReturn(events);

        MvcResult result = mockMvc.perform(get("/resource/vm/{vmId}/events", existingVmIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<EventGeneralDto> listOfDtos = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(listOfDtos);
        assertFalse(listOfDtos.isEmpty());
        assertEquals(3, listOfDtos.size());

        EventGeneralDto firstEvent = listOfDtos.getFirst();
        assertNotNull(firstEvent);
        assertEquals(event1.id(), firstEvent.id());
        assertEquals(event1.description(), firstEvent.message());
        assertEquals(event1.severity().name(), firstEvent.severity());
        assertEquals(event1.time(), Date.from(firstEvent.registeredAt().toInstant(ZoneOffset.UTC)));

        EventGeneralDto secondEvent = listOfDtos.get(1);
        assertNotNull(secondEvent);
        assertEquals(event2.id(), secondEvent.id());
        assertEquals(event2.description(), secondEvent.message());
        assertEquals(event2.severity().name(), secondEvent.severity());
        assertEquals(event2.time(), Date.from(secondEvent.registeredAt().toInstant(ZoneOffset.UTC)));

        EventGeneralDto thirdEvent = listOfDtos.getLast();
        assertNotNull(thirdEvent);
        assertEquals(event3.id(), thirdEvent.id());
        assertEquals(event3.description(), thirdEvent.message());
        assertEquals(event3.severity().name(), thirdEvent.severity());
        assertEquals(event3.time(), Date.from(thirdEvent.registeredAt().toInstant(ZoneOffset.UTC)));

        verify(oVirtVmService, times(1)).findVmById(Mockito.eq(existingVmIdentifier.toString()));
        verify(oVirtVmService, times(1)).findEventsByVmId(Mockito.eq(vm), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser
    public void Given_ExistingVmIdentifierIsPassedAndNoEventsWereForVm_When_FindEventsForVm_Then_ReturnsEmptyListOfEvents() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Vm vm = mock(Vm.class);

        when(oVirtVmService.findVmById(Mockito.eq(existingVmIdentifier.toString()))).thenReturn(vm);
        when(oVirtVmService.findEventsByVmId(Mockito.eq(vm), Mockito.eq(pageable))).thenReturn(List.of());

        mockMvc.perform(get("/resource/vm/{vmId}/events", existingVmIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(oVirtVmService, times(1)).findVmById(Mockito.eq(existingVmIdentifier.toString()));
        verify(oVirtVmService, times(1)).findEventsByVmId(Mockito.eq(vm), Mockito.eq(pageable));
    }

    @Test
    @WithMockUser
    public void Given_NonExistentVmIdentifierIsPassed_When_FindEventsForVm_Then_Returns404NotFound() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        when(oVirtVmService.findVmById(Mockito.eq(nonExistentVmIdentifier.toString())))
                .thenThrow(VmNotFoundException.class);

        mockMvc.perform(get("/resource/vm/{vmId}/events", nonExistentVmIdentifier)
                        .param("page", String.valueOf(pageNumber))
                        .param("size", String.valueOf(pageSize)))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(oVirtVmService, times(1))
                .findVmById(Mockito.eq(nonExistentVmIdentifier.toString()));
    }
}
