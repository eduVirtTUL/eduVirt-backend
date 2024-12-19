package pl.lodz.p.it.eduvirt.unit.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ovirt.engine.sdk4.types.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.lodz.p.it.eduvirt.aspect.exception.GeneralControllerExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.MaintenanceIntervalExceptionResolver;
import pl.lodz.p.it.eduvirt.aspect.exception.OVirtAPIExceptionResolver;
import pl.lodz.p.it.eduvirt.controller.MaintenanceIntervalController;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.CreateMaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDetailsDto;
import pl.lodz.p.it.eduvirt.dto.maintenance_interval.MaintenanceIntervalDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageDto;
import pl.lodz.p.it.eduvirt.dto.pagination.PageInfoDto;
import pl.lodz.p.it.eduvirt.entity.eduvirt.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.eduvirt.Updatable;
import pl.lodz.p.it.eduvirt.entity.eduvirt.reservation.MaintenanceInterval;
import pl.lodz.p.it.eduvirt.exceptions.ClusterNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalConflictException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalInvalidTimeWindowException;
import pl.lodz.p.it.eduvirt.exceptions.maintenance_interval.MaintenanceIntervalNotFound;
import pl.lodz.p.it.eduvirt.mappers.MaintenanceIntervalMapper;
import pl.lodz.p.it.eduvirt.service.MaintenanceIntervalService;
import pl.lodz.p.it.eduvirt.service.OVirtClusterService;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
        MaintenanceIntervalController.class,
        MaintenanceIntervalExceptionResolver.class,
        GeneralControllerExceptionResolver.class,
        OVirtAPIExceptionResolver.class
})
@WebMvcTest(controllers = {MaintenanceIntervalController.class}, useDefaultFilters = false)
public class MaintenanceIntervalControllerTest {

    @MockitoBean
    private MaintenanceIntervalService maintenanceIntervalService;

    @MockitoBean
    private OVirtClusterService clusterService;

    /* Mappers */

    @MockitoBean
    private MaintenanceIntervalMapper maintenanceIntervalMapper;

    /* Other */

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    /* Initialization */

    private final UUID existingClusterId = UUID.randomUUID();
    private final UUID nonExistentClusterId = UUID.randomUUID();

    private MaintenanceInterval maintenanceInterval1;
    private MaintenanceInterval maintenanceInterval2;
    private MaintenanceInterval maintenanceInterval3;
    private MaintenanceInterval maintenanceInterval4;

    private MaintenanceInterval maintenanceInterval5;
    private MaintenanceInterval maintenanceInterval6;
    private MaintenanceInterval maintenanceInterval7;
    private MaintenanceInterval maintenanceInterval8;

    @BeforeEach
    public void prepareTestData() throws Exception {
        mapper.findAndRegisterModules();

        maintenanceInterval1 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_1",
                "EXAMPLE_DESCRIPTION_1",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(4).toLocalDateTime()
        );

        maintenanceInterval2 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_2",
                "EXAMPLE_DESCRIPTION_2",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(6).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(10).toLocalDateTime()
        );

        maintenanceInterval3 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_3",
                "EXAMPLE_DESCRIPTION_3",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(12).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(16).toLocalDateTime()
        );

        maintenanceInterval4 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_4",
                "EXAMPLE_DESCRIPTION_4",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(2).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(2).toLocalDateTime()
        );

        maintenanceInterval5 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_5",
                "EXAMPLE_DESCRIPTION_5",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(4).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(2).toLocalDateTime()
        );

        maintenanceInterval6 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_6",
                "EXAMPLE_DESCRIPTION_6",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(10).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(6).toLocalDateTime()
        );

        maintenanceInterval7 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_7",
                "EXAMPLE_DESCRIPTION_7",
                MaintenanceInterval.IntervalType.CLUSTER,
                existingClusterId,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(16).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(14).toLocalDateTime()
        );

        maintenanceInterval8 = new MaintenanceInterval(
                "EXAMPLE_CAUSE_8",
                "EXAMPLE_DESCRIPTION_8",
                MaintenanceInterval.IntervalType.SYSTEM,
                null,
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(20).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).minusHours(18).toLocalDateTime()
        );

        Field id = AbstractEntity.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(maintenanceInterval1, UUID.randomUUID());
        id.set(maintenanceInterval2, UUID.randomUUID());
        id.set(maintenanceInterval3, UUID.randomUUID());
        id.set(maintenanceInterval4, UUID.randomUUID());
        id.set(maintenanceInterval5, UUID.randomUUID());
        id.set(maintenanceInterval6, UUID.randomUUID());
        id.set(maintenanceInterval7, UUID.randomUUID());
        id.set(maintenanceInterval8, UUID.randomUUID());
        id.setAccessible(false);

        Field version = Updatable.class.getDeclaredField("version");
        version.setAccessible(true);
        version.set(maintenanceInterval1, 1L);
        version.set(maintenanceInterval2, 1L);
        version.set(maintenanceInterval3, 1L);
        version.set(maintenanceInterval4, 1L);
        version.set(maintenanceInterval5, 1L);
        version.set(maintenanceInterval6, 1L);
        version.set(maintenanceInterval7, 1L);
        version.set(maintenanceInterval8, 1L);
        version.setAccessible(false);
    }

    /* Tests */

    /* CreateNewClusterMaintenanceInterval method tests */

    @WithMockUser
    @Test
    public void Given_ExistingClusterIdentifierIsPassed_When_CreateNewClusterMaintenanceInterval_Then_CreateNewClusterMaintenanceIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        Cluster cluster = mock(Cluster.class);
        when(clusterService.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doNothing().when(maintenanceIntervalService).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(maintenanceIntervalService, times(1)).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    @WithMockUser
    @Test
    public void Given_NonExistentClusterIdentifierIsPassed_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        when(clusterService.findClusterById(Mockito.eq(nonExistentClusterId)))
                .thenThrow(ClusterNotFoundException.class);

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", nonExistentClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(nonExistentClusterId));
    }

    @WithMockUser
    @Test
    public void Given_ServiceMethodThrowsMaintenanceIntervalInvalidTimeWindowException_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        Cluster cluster = mock(Cluster.class);
        when(clusterService.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doThrow(MaintenanceIntervalInvalidTimeWindowException.class).when(maintenanceIntervalService).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(maintenanceIntervalService, times(1)).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    @WithMockUser
    @Test
    public void Given_ServiceMethodThrowsMaintenanceIntervalConflictException_When_CreateNewClusterMaintenanceInterval_Then_Returns400BadRequest() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        Cluster cluster = mock(Cluster.class);
        when(clusterService.findClusterById(Mockito.eq(existingClusterId)))
                .thenReturn(cluster);

        doThrow(MaintenanceIntervalConflictException.class).when(maintenanceIntervalService).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/cluster/{clusterId}", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(clusterService, times(1)).findClusterById(Mockito.eq(existingClusterId));
        verify(maintenanceIntervalService, times(1)).createClusterMaintenanceInterval(
                Mockito.eq(cluster),
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    /* CreateNewSystemMaintenanceInterval method tests */

    @WithMockUser
    @Test
    public void Given_AllDataIsValid_When_CreateNewSystemMaintenanceInterval_Then_CreateNewSystemMaintenanceIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        doNothing().when(maintenanceIntervalService).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/system")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(maintenanceIntervalService, times(1)).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    @WithMockUser
    @Test
    public void Given_ServiceMethodThrowsMaintenanceIntervalInvalidTimeWindowException_When_CreateNewSystemMaintenanceInterval_Then_CreateNewSystemMaintenanceIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        doThrow(MaintenanceIntervalInvalidTimeWindowException.class).when(maintenanceIntervalService).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/system", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(maintenanceIntervalService, times(1)).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    @WithMockUser
    @Test
    public void Given_ServiceMethodThrowsMaintenanceIntervalConflictException_When_CreateNewSystemMaintenanceInterval_Then_CreateNewSystemMaintenanceIntervalSuccessfully() throws Exception {
        CreateMaintenanceIntervalDto createDto = new CreateMaintenanceIntervalDto(
                "EXAMPLE_CAUSE", "EXAMPLE_DESCRIPTION",
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(1).toLocalDateTime(),
                OffsetDateTime.now(ZoneOffset.UTC).plusHours(3).toLocalDateTime()
        );

        doThrow(MaintenanceIntervalConflictException.class).when(maintenanceIntervalService).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );

        mockMvc.perform(post("/maintenance-intervals/system", existingClusterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isConflict());

        verify(maintenanceIntervalService, times(1)).createSystemMaintenanceInterval(
                Mockito.eq(createDto.cause()),
                Mockito.eq(createDto.description()),
                Mockito.eq(createDto.beginAt()),
                Mockito.eq(createDto.endAt())
        );
    }

    /* GetAllMaintenanceIntervals method tests */

    @WithMockUser
    @Test
    public void Given_SomeActiveClusterMaintenanceIntervalsExistInTheEduVirtDB_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        UUID clusterId = existingClusterId;
        boolean active = true;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        MaintenanceIntervalDto dtoNo1 = new MaintenanceIntervalDto(
                maintenanceInterval1.getId(),
                maintenanceInterval1.getCause(),
                maintenanceInterval1.getDescription(),
                maintenanceInterval1.getType().toString(),
                maintenanceInterval1.getClusterId(),
                maintenanceInterval1.getBeginAt(),
                maintenanceInterval1.getEndAt()
        );

        MaintenanceIntervalDto dtoNo2 = new MaintenanceIntervalDto(
                maintenanceInterval3.getId(),
                maintenanceInterval3.getCause(),
                maintenanceInterval3.getDescription(),
                maintenanceInterval3.getType().toString(),
                maintenanceInterval3.getClusterId(),
                maintenanceInterval3.getBeginAt(),
                maintenanceInterval3.getEndAt()
        );

        when(maintenanceIntervalService.findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval1, maintenanceInterval3), pageable, 2));

        when(maintenanceIntervalMapper.maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class)))
                .thenReturn(dtoNo1, dtoNo2);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("clusterId", clusterId.toString())
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();

        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = foundPage.items();
        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);
        assertEquals(firstMaintenanceInterval.id(), maintenanceInterval1.getId());
        assertEquals(firstMaintenanceInterval.cause(), maintenanceInterval1.getCause());
        assertEquals(firstMaintenanceInterval.description(), maintenanceInterval1.getDescription());
        assertEquals(firstMaintenanceInterval.type(), maintenanceInterval1.getType().toString());
        assertEquals(firstMaintenanceInterval.clusterId(), maintenanceInterval1.getClusterId());
        assertEquals(firstMaintenanceInterval.beginAt(), maintenanceInterval1.getBeginAt());
        assertEquals(firstMaintenanceInterval.endAt(), maintenanceInterval1.getEndAt());

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
        assertEquals(secondMaintenanceInterval.id(), maintenanceInterval3.getId());
        assertEquals(secondMaintenanceInterval.cause(), maintenanceInterval3.getCause());
        assertEquals(secondMaintenanceInterval.description(), maintenanceInterval3.getDescription());
        assertEquals(secondMaintenanceInterval.type(), maintenanceInterval3.getType().toString());
        assertEquals(secondMaintenanceInterval.clusterId(), maintenanceInterval3.getClusterId());
        assertEquals(secondMaintenanceInterval.beginAt(), maintenanceInterval3.getBeginAt());
        assertEquals(secondMaintenanceInterval.endAt(), maintenanceInterval3.getEndAt());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable));

        verify(maintenanceIntervalMapper, times(2))
                .maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class));
    }

    @WithMockUser
    @Test
    public void Given_SomeActiveSystemMaintenanceIntervalsExistInTheEduVirtDB_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        boolean active = true;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        MaintenanceIntervalDto dtoNo1 = new MaintenanceIntervalDto(
                maintenanceInterval2.getId(),
                maintenanceInterval2.getCause(),
                maintenanceInterval2.getDescription(),
                maintenanceInterval2.getType().toString(),
                maintenanceInterval2.getClusterId(),
                maintenanceInterval2.getBeginAt(),
                maintenanceInterval2.getEndAt()
        );

        MaintenanceIntervalDto dtoNo2 = new MaintenanceIntervalDto(
                maintenanceInterval4.getId(),
                maintenanceInterval4.getCause(),
                maintenanceInterval4.getDescription(),
                maintenanceInterval4.getType().toString(),
                maintenanceInterval4.getClusterId(),
                maintenanceInterval4.getBeginAt(),
                maintenanceInterval4.getEndAt()
        );

        when(maintenanceIntervalService.findAllMaintenanceIntervals(Mockito.isNull(), Mockito.eq(active), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval2, maintenanceInterval4), pageable, 2));

        when(maintenanceIntervalMapper.maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class)))
                .thenReturn(dtoNo1, dtoNo2);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();

        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = foundPage.items();
        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);
        assertEquals(firstMaintenanceInterval.id(), maintenanceInterval2.getId());
        assertEquals(firstMaintenanceInterval.cause(), maintenanceInterval2.getCause());
        assertEquals(firstMaintenanceInterval.description(), maintenanceInterval2.getDescription());
        assertEquals(firstMaintenanceInterval.type(), maintenanceInterval2.getType().toString());
        assertEquals(firstMaintenanceInterval.clusterId(), maintenanceInterval2.getClusterId());
        assertEquals(firstMaintenanceInterval.beginAt(), maintenanceInterval2.getBeginAt());
        assertEquals(firstMaintenanceInterval.endAt(), maintenanceInterval2.getEndAt());

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
        assertEquals(secondMaintenanceInterval.id(), maintenanceInterval4.getId());
        assertEquals(secondMaintenanceInterval.cause(), maintenanceInterval4.getCause());
        assertEquals(secondMaintenanceInterval.description(), maintenanceInterval4.getDescription());
        assertEquals(secondMaintenanceInterval.type(), maintenanceInterval4.getType().toString());
        assertEquals(secondMaintenanceInterval.clusterId(), maintenanceInterval4.getClusterId());
        assertEquals(secondMaintenanceInterval.beginAt(), maintenanceInterval4.getBeginAt());
        assertEquals(secondMaintenanceInterval.endAt(), maintenanceInterval4.getEndAt());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervals(Mockito.isNull(), Mockito.eq(active), Mockito.eq(pageable));

        verify(maintenanceIntervalMapper, times(2))
                .maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class));
    }

    @WithMockUser
    @Test
    public void Given_SomeInactiveClusterMaintenanceIntervalsExistInTheEduVirtDB_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        UUID clusterId = existingClusterId;
        boolean active = false;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        MaintenanceIntervalDto dtoNo1 = new MaintenanceIntervalDto(
                maintenanceInterval5.getId(),
                maintenanceInterval5.getCause(),
                maintenanceInterval5.getDescription(),
                maintenanceInterval5.getType().toString(),
                maintenanceInterval5.getClusterId(),
                maintenanceInterval5.getBeginAt(),
                maintenanceInterval5.getEndAt()
        );

        MaintenanceIntervalDto dtoNo2 = new MaintenanceIntervalDto(
                maintenanceInterval7.getId(),
                maintenanceInterval7.getCause(),
                maintenanceInterval7.getDescription(),
                maintenanceInterval7.getType().toString(),
                maintenanceInterval7.getClusterId(),
                maintenanceInterval7.getBeginAt(),
                maintenanceInterval7.getEndAt()
        );

        when(maintenanceIntervalService.findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval5, maintenanceInterval7), pageable, 2));

        when(maintenanceIntervalMapper.maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class)))
                .thenReturn(dtoNo1, dtoNo2);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("clusterId", clusterId.toString())
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();

        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = foundPage.items();
        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);
        assertEquals(firstMaintenanceInterval.id(), maintenanceInterval5.getId());
        assertEquals(firstMaintenanceInterval.cause(), maintenanceInterval5.getCause());
        assertEquals(firstMaintenanceInterval.description(), maintenanceInterval5.getDescription());
        assertEquals(firstMaintenanceInterval.type(), maintenanceInterval5.getType().toString());
        assertEquals(firstMaintenanceInterval.clusterId(), maintenanceInterval5.getClusterId());
        assertEquals(firstMaintenanceInterval.beginAt(), maintenanceInterval5.getBeginAt());
        assertEquals(firstMaintenanceInterval.endAt(), maintenanceInterval5.getEndAt());

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
        assertEquals(secondMaintenanceInterval.id(), maintenanceInterval7.getId());
        assertEquals(secondMaintenanceInterval.cause(), maintenanceInterval7.getCause());
        assertEquals(secondMaintenanceInterval.description(), maintenanceInterval7.getDescription());
        assertEquals(secondMaintenanceInterval.type(), maintenanceInterval7.getType().toString());
        assertEquals(secondMaintenanceInterval.clusterId(), maintenanceInterval7.getClusterId());
        assertEquals(secondMaintenanceInterval.beginAt(), maintenanceInterval7.getBeginAt());
        assertEquals(secondMaintenanceInterval.endAt(), maintenanceInterval7.getEndAt());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable));

        verify(maintenanceIntervalMapper, times(2))
                .maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class));
    }

    @WithMockUser
    @Test
    public void Given_SomeInactiveSystemMaintenanceIntervalsExistInTheEduVirtDB_When_GetAllMaintenanceIntervals_Then_ReturnsAllFoundMaintenanceIntervals() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        boolean active = true;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        MaintenanceIntervalDto dtoNo1 = new MaintenanceIntervalDto(
                maintenanceInterval6.getId(),
                maintenanceInterval6.getCause(),
                maintenanceInterval6.getDescription(),
                maintenanceInterval6.getType().toString(),
                maintenanceInterval6.getClusterId(),
                maintenanceInterval6.getBeginAt(),
                maintenanceInterval6.getEndAt()
        );

        MaintenanceIntervalDto dtoNo2 = new MaintenanceIntervalDto(
                maintenanceInterval8.getId(),
                maintenanceInterval8.getCause(),
                maintenanceInterval8.getDescription(),
                maintenanceInterval8.getType().toString(),
                maintenanceInterval8.getClusterId(),
                maintenanceInterval8.getBeginAt(),
                maintenanceInterval8.getEndAt()
        );

        when(maintenanceIntervalService.findAllMaintenanceIntervals(Mockito.isNull(), Mockito.eq(active), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(maintenanceInterval6, maintenanceInterval8), pageable, 2));

        when(maintenanceIntervalMapper.maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class)))
                .thenReturn(dtoNo1, dtoNo2);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        PageDto<MaintenanceIntervalDto> foundPage = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundPage);
        assertNotNull(foundPage.page());
        assertNotNull(foundPage.items());

        PageInfoDto pageInfo = foundPage.page();

        assertNotNull(pageInfo);
        assertEquals(pageInfo.page(), 0);
        assertEquals(pageInfo.elements(), 2);
        assertEquals(pageInfo.totalPages(), 1);
        assertEquals(pageInfo.totalElements(), 2);

        List<MaintenanceIntervalDto> foundMaintenanceIntervals = foundPage.items();
        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);
        assertEquals(firstMaintenanceInterval.id(), maintenanceInterval6.getId());
        assertEquals(firstMaintenanceInterval.cause(), maintenanceInterval6.getCause());
        assertEquals(firstMaintenanceInterval.description(), maintenanceInterval6.getDescription());
        assertEquals(firstMaintenanceInterval.type(), maintenanceInterval6.getType().toString());
        assertEquals(firstMaintenanceInterval.clusterId(), maintenanceInterval6.getClusterId());
        assertEquals(firstMaintenanceInterval.beginAt(), maintenanceInterval6.getBeginAt());
        assertEquals(firstMaintenanceInterval.endAt(), maintenanceInterval6.getEndAt());

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
        assertEquals(secondMaintenanceInterval.id(), maintenanceInterval8.getId());
        assertEquals(secondMaintenanceInterval.cause(), maintenanceInterval8.getCause());
        assertEquals(secondMaintenanceInterval.description(), maintenanceInterval8.getDescription());
        assertEquals(secondMaintenanceInterval.type(), maintenanceInterval8.getType().toString());
        assertEquals(secondMaintenanceInterval.clusterId(), maintenanceInterval8.getClusterId());
        assertEquals(secondMaintenanceInterval.beginAt(), maintenanceInterval8.getBeginAt());
        assertEquals(secondMaintenanceInterval.endAt(), maintenanceInterval8.getEndAt());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervals(Mockito.isNull(), Mockito.eq(active), Mockito.eq(pageable));

        verify(maintenanceIntervalMapper, times(2))
                .maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class));
    }

    @WithMockUser
    @Test
    public void Given_NoMaintenanceIntervalsExistInTheEduVirtDB_When_GetAllMaintenanceIntervals_Then_ReturnsEmptyMaintenanceIntervalList() throws Exception {
        int pageNumber = 0;
        int pageSize = 10;

        UUID clusterId = existingClusterId;
        boolean active = false;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        when(maintenanceIntervalService.findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("clusterId", clusterId.toString())
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervals(Mockito.eq(clusterId), Mockito.eq(active), Mockito.eq(pageable));
    }

    @WithMockUser
    @Test
    public void Given_InvalidPaginationParametersArePassed_When_GetAllMaintenanceIntervals_Then_ReturnsEmptyMaintenanceIntervalList() throws Exception {
        int pageNumber = -1;
        int pageSize = 10;
        boolean active = false;

        mockMvc.perform(get("/maintenance-intervals")
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize))
                        .param("clusterId", existingClusterId.toString())
                        .param("active", String.valueOf(active)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /* GetMaintenanceIntervalsWithinTimePeriod method tests */

    @WithMockUser
    @Test
    public void Given_SomeMaintenanceIntervalsExistInSelectedTimePeriod_When_GetMaintenanceIntervalsWithinTimePeriod_Then_ReturnsAllFoundMaintenanceIntervalsWithinTimePeriod() throws Exception {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(12).toLocalDateTime();

        MaintenanceIntervalDto dtoNo1 = new MaintenanceIntervalDto(
                maintenanceInterval4.getId(),
                maintenanceInterval4.getCause(),
                maintenanceInterval4.getDescription(),
                maintenanceInterval4.getType().toString(),
                maintenanceInterval4.getClusterId(),
                maintenanceInterval4.getBeginAt(),
                maintenanceInterval4.getEndAt()
        );

        MaintenanceIntervalDto dtoNo2 = new MaintenanceIntervalDto(
                maintenanceInterval1.getId(),
                maintenanceInterval1.getCause(),
                maintenanceInterval1.getDescription(),
                maintenanceInterval1.getType().toString(),
                maintenanceInterval1.getClusterId(),
                maintenanceInterval1.getBeginAt(),
                maintenanceInterval1.getEndAt()
        );

        when(maintenanceIntervalService.findAllMaintenanceIntervalsInTimePeriod(Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of(maintenanceInterval4, maintenanceInterval1));

        when(maintenanceIntervalMapper.maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class)))
                .thenReturn(dtoNo1, dtoNo2);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals/time-period")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        List<MaintenanceIntervalDto> foundMaintenanceIntervals = mapper.readValue(json, new TypeReference<>() {});

        assertNotNull(foundMaintenanceIntervals);
        assertFalse(foundMaintenanceIntervals.isEmpty());
        assertEquals(2, foundMaintenanceIntervals.size());

        MaintenanceIntervalDto firstMaintenanceInterval = foundMaintenanceIntervals.getFirst();
        assertNotNull(firstMaintenanceInterval);
        assertEquals(firstMaintenanceInterval.id(), maintenanceInterval4.getId());
        assertEquals(firstMaintenanceInterval.cause(), maintenanceInterval4.getCause());
        assertEquals(firstMaintenanceInterval.description(), maintenanceInterval4.getDescription());
        assertEquals(firstMaintenanceInterval.type(), maintenanceInterval4.getType().toString());
        assertEquals(firstMaintenanceInterval.clusterId(), maintenanceInterval4.getClusterId());
        assertEquals(firstMaintenanceInterval.beginAt(), maintenanceInterval4.getBeginAt());
        assertEquals(firstMaintenanceInterval.endAt(), maintenanceInterval4.getEndAt());

        MaintenanceIntervalDto secondMaintenanceInterval = foundMaintenanceIntervals.getLast();
        assertNotNull(secondMaintenanceInterval);
        assertEquals(secondMaintenanceInterval.id(), maintenanceInterval1.getId());
        assertEquals(secondMaintenanceInterval.cause(), maintenanceInterval1.getCause());
        assertEquals(secondMaintenanceInterval.description(), maintenanceInterval1.getDescription());
        assertEquals(secondMaintenanceInterval.type(), maintenanceInterval1.getType().toString());
        assertEquals(secondMaintenanceInterval.clusterId(), maintenanceInterval1.getClusterId());
        assertEquals(secondMaintenanceInterval.beginAt(), maintenanceInterval1.getBeginAt());
        assertEquals(secondMaintenanceInterval.endAt(), maintenanceInterval1.getEndAt());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervalsInTimePeriod(Mockito.eq(start), Mockito.eq(end));

        verify(maintenanceIntervalMapper, times(2))
                .maintenanceIntervalToDto(Mockito.any(MaintenanceInterval.class));
    }

    @WithMockUser
    @Test
    public void Given_NoMaintenanceIntervalsExistInSelectedTimePeriod_When_GetMaintenanceIntervalsWithinTimePeriod_Then_ReturnsEmptyMaintenanceIntervalList() throws Exception {
        LocalDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusHours(24).toLocalDateTime();
        LocalDateTime end = OffsetDateTime.now(ZoneOffset.UTC).plusHours(48).toLocalDateTime();

        when(maintenanceIntervalService.findAllMaintenanceIntervalsInTimePeriod(Mockito.eq(start), Mockito.eq(end)))
                .thenReturn(List.of());

        mockMvc.perform(get("/maintenance-intervals/time-period")
                        .param("start", start.toString())
                        .param("end", end.toString()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(maintenanceIntervalService, times(1))
                .findAllMaintenanceIntervalsInTimePeriod(Mockito.eq(start), Mockito.eq(end));
    }

    /* GetMaintenanceInterval method tests */

    @WithMockUser
    @Test
    public void Given_ExistingMaintenanceIntervalIsPassed_When_GetMaintenanceInterval_Then_ReturnsFoundMaintenanceInterval() throws Exception {
        MaintenanceIntervalDetailsDto maintenanceInterval = new MaintenanceIntervalDetailsDto(
                maintenanceInterval1.getId(),
                maintenanceInterval1.getCause(),
                maintenanceInterval1.getDescription(),
                maintenanceInterval1.getType().toString(),
                maintenanceInterval1.getClusterId(),
                maintenanceInterval1.getBeginAt(),
                maintenanceInterval1.getEndAt()
        );

        when(maintenanceIntervalService.findMaintenanceInterval(Mockito.eq(maintenanceInterval1.getId())))
                .thenReturn(Optional.of(maintenanceInterval1));

        when(maintenanceIntervalMapper.maintenanceIntervalToDetailsDto(Mockito.eq(maintenanceInterval1)))
                .thenReturn(maintenanceInterval);

        MvcResult result = mockMvc.perform(get("/maintenance-intervals/{intervalId}", maintenanceInterval1.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        MaintenanceIntervalDetailsDto foundMaintenanceInterval = mapper.readValue(json, MaintenanceIntervalDetailsDto.class);

        assertNotNull(foundMaintenanceInterval);
        assertNotNull(foundMaintenanceInterval.id());
        assertNotNull(foundMaintenanceInterval.cause());
        assertNotNull(foundMaintenanceInterval.description());
        assertNotNull(foundMaintenanceInterval.type());
        assertNotNull(foundMaintenanceInterval.clusterId());
        assertNotNull(foundMaintenanceInterval.beginAt());
        assertNotNull(foundMaintenanceInterval.endAt());

        assertEquals(foundMaintenanceInterval.id(), maintenanceInterval1.getId());
        assertEquals(foundMaintenanceInterval.cause(), maintenanceInterval1.getCause());
        assertEquals(foundMaintenanceInterval.description(), maintenanceInterval1.getDescription());
        assertEquals(foundMaintenanceInterval.type(), maintenanceInterval1.getType().toString());
        assertEquals(foundMaintenanceInterval.clusterId(), maintenanceInterval1.getClusterId());
        assertEquals(foundMaintenanceInterval.beginAt(), maintenanceInterval1.getBeginAt());
        assertEquals(foundMaintenanceInterval.endAt(), maintenanceInterval1.getEndAt());

        assertTrue(foundMaintenanceInterval.endAt().isAfter(foundMaintenanceInterval.beginAt()));

        verify(maintenanceIntervalService, times(1))
                .findMaintenanceInterval(Mockito.eq(maintenanceInterval1.getId()));

        verify(maintenanceIntervalMapper, times(1))
                .maintenanceIntervalToDetailsDto(Mockito.eq(maintenanceInterval1));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMaintenanceIntervalIsPassed_When_GetMaintenanceInterval_Then_Returns404NotFound() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        when(maintenanceIntervalService.findMaintenanceInterval(Mockito.eq(randomUUID)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/maintenance-intervals/{intervalId}", randomUUID))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(maintenanceIntervalService, times(1))
                .findMaintenanceInterval(Mockito.eq(randomUUID));
    }

    /* FinishMaintenanceInterval method tests */

    @WithMockUser
    @Test
    public void Given_ExistingMaintenanceIntervalIsPassed_When_FinishMaintenanceInterval_Then_FinishesGivenMaintenanceIntervalSuccessfully() throws Exception {
        doNothing().when(maintenanceIntervalService)
                .finishMaintenanceInterval(Mockito.eq(maintenanceInterval1.getId()));

        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", maintenanceInterval1.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(maintenanceIntervalService, times(1))
                .finishMaintenanceInterval(Mockito.eq(maintenanceInterval1.getId()));
    }

    @WithMockUser
    @Test
    public void Given_NonExistentMaintenanceIntervalIsPassed_When_FinishMaintenanceInterval_Then_FinishesGivenMaintenanceIntervalSuccessfully() throws Exception {
        UUID randomUUID = UUID.randomUUID();
        doThrow(MaintenanceIntervalNotFound.class).when(maintenanceIntervalService)
                .finishMaintenanceInterval(Mockito.eq(randomUUID));

        mockMvc.perform(delete("/maintenance-intervals/{intervalId}", randomUUID)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(maintenanceIntervalService, times(1))
                .finishMaintenanceInterval(Mockito.eq(randomUUID));
    }
}
