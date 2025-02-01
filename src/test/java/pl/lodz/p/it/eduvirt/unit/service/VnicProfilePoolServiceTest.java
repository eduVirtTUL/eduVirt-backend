package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.sdk4.builders.NetworkBuilder;
import org.ovirt.engine.sdk4.builders.VlanBuilder;
import org.ovirt.engine.sdk4.builders.VnicProfileBuilder;
import org.ovirt.engine.sdk4.types.VnicProfile;
import org.springframework.data.domain.Pageable;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.network.VlansRange;
import pl.lodz.p.it.eduvirt.entity.network.VnicProfilePoolMember;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileCurrentlyInUseException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileEduvirtNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.VnicProfileOvirtNotFoundException;
import pl.lodz.p.it.eduvirt.repository.VlansRangeRepository;
import pl.lodz.p.it.eduvirt.repository.VnicProfileRepository;
import pl.lodz.p.it.eduvirt.service.VnicProfilePoolService;
import pl.lodz.p.it.eduvirt.service.impl.VnicProfilePoolServiceImpl;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVnicProfileService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

//todo rewrite then in tests names

@ExtendWith(MockitoExtension.class)
public class VnicProfilePoolServiceTest {

    /* Repositories */

    @Mock
    private VnicProfileRepository vnicProfileRepository;
    @Mock
    private VlansRangeRepository vlansRangeRepository;

    /* Services */
    @Mock
    private OVirtVnicProfileService oVirtVnicProfileService;

    /* Inject mocks */
    @InjectMocks
    private VnicProfilePoolServiceImpl vnicProfilePoolService;

    /* Test data */

    private VlansRange vlansRange1;
    private VlansRange vlansRange2;

    private UUID vnicProfileId1;
    private UUID vnicProfileId2;
    private UUID vnicProfileId3;

    private VnicProfile oVirtVnicProfile1;
    private VnicProfile oVirtVnicProfile2;
    private VnicProfile oVirtVnicProfile3;

    private VnicProfilePoolMember eduVirtVnicProfile1;
    private VnicProfilePoolMember eduVirtVnicProfile2;
    private VnicProfilePoolMember eduVirtVnicProfile3;

    /* Constants */

    private final String vnicProfileName1 = "testVnicProfile1";
    private final String networkName1 = "testNetwork1";
    private final int vlanId1 = 1;

    private final String vnicProfileName2 = "testVnicProfile2";
    private final String networkName2 = "testNetwork2";
    private final int vlanId2 = 2;

    private final String vnicProfileName3 = "testVnicProfile3";
    private final String networkName3 = "testNetwork3";
    private final int vlanId3 = 3;


    /* Data initialization */

    @BeforeEach
    void setUp() {
        initVlanRanges();
        initIds();
        initOvirtVnicProfiles();
        initEduVirtVnicProfiles();
    }

    private void initVlanRanges() {
        vlansRange1 = new VlansRange(0, 10);
        vlansRange2 = new VlansRange(50, 60);
    }

    private void initIds() {
        vnicProfileId1 = UUID.randomUUID();
        vnicProfileId2 = UUID.randomUUID();
        vnicProfileId3 = UUID.randomUUID();
    }

    private void initOvirtVnicProfiles() {
        oVirtVnicProfile1 = new VnicProfileBuilder()
                .id(vnicProfileId1.toString())
                .name(vnicProfileName1)
                .network(
                        new NetworkBuilder()
                                .id(UUID.randomUUID().toString())
                                .name(networkName1)
                                .vlan(
                                        new VlanBuilder().id(vlanId1)
                                )
                ).build();

        oVirtVnicProfile2 = new VnicProfileBuilder()
                .id(vnicProfileId2.toString())
                .name(vnicProfileName2)
                .network(
                        new NetworkBuilder()
                                .id(UUID.randomUUID().toString())
                                .name(networkName2)
                                .vlan(
                                        new VlanBuilder().id(vlanId2)
                                )
                ).build();

        oVirtVnicProfile3 = new VnicProfileBuilder()
                .id(vnicProfileId3.toString())
                .name(vnicProfileName3)
                .network(
                        new NetworkBuilder()
                                .id(UUID.randomUUID().toString())
                                .name(networkName3)
                                .vlan(
                                        new VlanBuilder().id(vlanId3)
                                )
                ).build();
    }

    private void initEduVirtVnicProfiles() {
        eduVirtVnicProfile1 = new VnicProfilePoolMember(
                vnicProfileId1,
                vlanId1,
                vnicProfileName1,
                networkName1
        );

        eduVirtVnicProfile2 = new VnicProfilePoolMember(
                vnicProfileId2,
                vlanId2,
                vnicProfileName2,
                networkName2
        );

        eduVirtVnicProfile3 = new VnicProfilePoolMember(
                vnicProfileId3,
                vlanId3,
                vnicProfileName3,
                networkName3
        );
    }

    /* Tests */

    @Test
    void Given_VnicProfilesExist_When_GetSynchronized_Then_ReturnAllVlansRanges() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1,
                vlansRange2
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<VnicProfile> ovirtVnicProfileList = List.of(
                oVirtVnicProfile1,
                oVirtVnicProfile2,
                oVirtVnicProfile3
        );

        when(oVirtVnicProfileService.getVnicProfiles(Pageable.unpaged(), vlansRangeList.toArray(new VlansRange[0])))
                .thenReturn(ovirtVnicProfileList);

        Set<UUID> ovirtVnicProfileIdSet = Set.of(
                vnicProfileId1,
                vnicProfileId2,
                vnicProfileId3
        );

        List<VnicProfilePoolMember> eduVirtVnicProfileList = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllWithIds(ovirtVnicProfileIdSet))
                .thenReturn(eduVirtVnicProfileList);

        // Check aggregated lists
        VnicProfilePoolService.VnicProfilesAggregate vnicProfilesAggregate =
                vnicProfilePoolService.getSynchronizedVnicProfiles(Pageable.unpaged());

        assertNotNull(vnicProfilesAggregate);

        // Check vnic profile in the pool list
        List<VnicProfilePoolMember> vnicProfilesInPool = vnicProfilesAggregate.inPool();

        assertNotNull(vnicProfilesInPool);
        assertEquals(2, vnicProfilesInPool.size());
        assertTrue(vnicProfilesInPool.contains(eduVirtVnicProfile1));
        assertTrue(vnicProfilesInPool.contains(eduVirtVnicProfile2));

        // Check vnic profile out of the pool list
        List<VnicProfile> vnicProfilesOutPool = vnicProfilesAggregate.outOfPool();

        assertNotNull(vnicProfilesOutPool);
        assertEquals(1, vnicProfilesOutPool.size());
        assertEquals(vnicProfilesOutPool.getFirst(), oVirtVnicProfile3);
    }

    @Test
    void Given_VnicProfilesExist_When_GetVnicProfilesPool_Then_ReturnAllVlansRanges() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<VnicProfilePoolMember> vnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllWithVlanIds(
                vlansToInclude,
                Pageable.unpaged()
        )).thenReturn(vnicProfilesPool);

        // Mock fetching vnic profiles by ids from oVirt
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        List<VnicProfilePoolMember> result = vnicProfilePoolService.getVnicProfilesPool(Pageable.unpaged());

        // Check whole pool
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile1));
        assertTrue(eduVirtVnicProfile1.isValid());
        assertTrue(eduVirtVnicProfile1.getValidationErrors().isEmpty());

        // Check second vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile2));
        assertTrue(eduVirtVnicProfile2.isValid());
        assertTrue(eduVirtVnicProfile2.getValidationErrors().isEmpty());
    }

    @Test
    void Given_VnicProfilesExistButHaveDivergentData_When_GetVnicProfilesPool_Then_ReturnAllVlansRanges() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Network name value corruption
        eduVirtVnicProfile2 = new VnicProfilePoolMember(
                vnicProfileId2,
                vlanId2,
                vnicProfileName2,
                networkName2 + "FAKE"
        );

        List<VnicProfilePoolMember> vnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllWithVlanIds(
                vlansToInclude,
                Pageable.unpaged()
        )).thenReturn(vnicProfilesPool);

        // Mock fetching vnic profiles by ids from oVirt
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        List<VnicProfilePoolMember> result = vnicProfilePoolService.getVnicProfilesPool(Pageable.unpaged());

        // Check whole pool
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile1));
        assertTrue(eduVirtVnicProfile1.isValid());
        assertTrue(eduVirtVnicProfile1.getValidationErrors().isEmpty());

        // Check second vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile2));
        assertFalse(eduVirtVnicProfile2.isValid());
        assertFalse(eduVirtVnicProfile2.getValidationErrors().isEmpty());
    }

    @Test
    void Given_SomeVnicProfilesNotExistInOvirt_When_GetVnicProfilesPool_Then_ReturnAllVlansRanges() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<VnicProfilePoolMember> vnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllWithVlanIds(
                vlansToInclude,
                Pageable.unpaged()
        )).thenReturn(vnicProfilesPool);

        // Mock fetching vnic profiles by ids from oVirt
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenThrow(new VnicProfileOvirtNotFoundException(vnicProfileId1));

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        List<VnicProfilePoolMember> result = vnicProfilePoolService.getVnicProfilesPool(Pageable.unpaged());

        // Check whole pool
        assertNotNull(result);
        assertEquals(2, result.size());

        // Check first vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile1));
        assertFalse(eduVirtVnicProfile1.isValid());
        assertTrue(eduVirtVnicProfile1.getValidationErrors().isEmpty());

        // Check second vnic profile from pool
        assertTrue(result.contains(eduVirtVnicProfile2));
        assertTrue(eduVirtVnicProfile2.isValid());
        assertTrue(eduVirtVnicProfile2.getValidationErrors().isEmpty());
    }

    @Test
    void Given_VnicProfileExists_When_GetVnicProfileFromPool_Then_ReturnVnicProfileFromPool() {
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        VnicProfilePoolMember result = vnicProfilePoolService.getVnicProfileFromPool(vnicProfileId1);

        assertNotNull(result);
        assertEquals(eduVirtVnicProfile1, result);
    }

    @Test
    void Given_NoVnicProfileExistsInOvirt_When_GetVnicProfileFromPool_Then_ReturnVnicProfileFromPool() {
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(null);

        assertThrows(VnicProfileOvirtNotFoundException.class,
                () -> vnicProfilePoolService.getVnicProfileFromPool(vnicProfileId1)
        );
    }

    @Test
    void Given_VnicProfileExistsInOvirtButNoExistsInEduVirt_When_GetVnicProfileFromPool_Then_ReturnVnicProfileFromPool() {
        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        assertThrows(VnicProfileEduvirtNotFoundException.class,
                () -> vnicProfilePoolService.getVnicProfileFromPool(vnicProfileId1)
        );
    }

    @Test
    void Given_FirstFreeVnicProfileFromPoolIsValid_When_GetFirstFreeVnicProfileFromPool_Then_ReturnOptionalVnicProfileFromPool() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<VnicProfilePoolMember> freeVnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude))
                .thenReturn(freeVnicProfilesPool);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        Optional<VnicProfilePoolMember> result = vnicProfilePoolService.getFirstFreeVnicProfileFromPool();

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(eduVirtVnicProfile1, result.get());

        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
        verify(oVirtVnicProfileService, times(0)).getVnicProfileById(vnicProfileId2.toString());
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(any(String.class));
    }

    @Test
    void Given_NoFreeVnicProfilesExist_When_GetFirstFreeVnicProfileFromPool_Then_ReturnOptionalVnicProfileFromPool() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        when(vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude))
                .thenReturn(List.of());

        Optional<VnicProfilePoolMember> result = vnicProfilePoolService.getFirstFreeVnicProfileFromPool();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(oVirtVnicProfileService, times(0)).getVnicProfileById(any(String.class));
    }

    @Test
    void Given_FirstFreeVnicProfileFromPoolIsNotValidButSecondIs_When_GetFirstFreeVnicProfileFromPool_Then_ReturnOptionalVnicProfileFromPool() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Network name value corruption
        eduVirtVnicProfile1 = new VnicProfilePoolMember(
                vnicProfileId1,
                vlanId1,
                vnicProfileName1,
                networkName1 + "FAKE"
        );

        List<VnicProfilePoolMember> freeVnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude))
                .thenReturn(freeVnicProfilesPool);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        Optional<VnicProfilePoolMember> result = vnicProfilePoolService.getFirstFreeVnicProfileFromPool();

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(eduVirtVnicProfile2, result.get());

        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId2.toString());
        verify(oVirtVnicProfileService, times(2)).getVnicProfileById(any(String.class));
    }

    @Test
    void Given_FirstFreeVnicProfileFromPoolIsNullInOvirtButSecondIs_When_GetFirstFreeVnicProfileFromPool_Then_ReturnOptionalVnicProfileFromPool() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<VnicProfilePoolMember> freeVnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude))
                .thenReturn(freeVnicProfilesPool);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(null);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        Optional<VnicProfilePoolMember> result = vnicProfilePoolService.getFirstFreeVnicProfileFromPool();

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(eduVirtVnicProfile2, result.get());

        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId2.toString());
        verify(oVirtVnicProfileService, times(2)).getVnicProfileById(any(String.class));
    }

    @Test
    void Given_FirstFreeVnicProfileFromPoolNoExistInOvirtButSecondIs_When_GetFirstFreeVnicProfileFromPool_Then_ReturnOptionalVnicProfileFromPool() {
        List<VlansRange> vlansRangeList = List.of(
                vlansRange1
        );

        when(vlansRangeRepository.findAll())
                .thenReturn(vlansRangeList);

        List<Integer> vlansToInclude = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        List<VnicProfilePoolMember> freeVnicProfilesPool = List.of(
                eduVirtVnicProfile1,
                eduVirtVnicProfile2
        );

        when(vnicProfileRepository.findAllFreeVnicProfiles(vlansToInclude))
                .thenReturn(freeVnicProfilesPool);

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenThrow(new VnicProfileOvirtNotFoundException(vnicProfileId1));

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId2.toString()))
                .thenReturn(oVirtVnicProfile2);

        Optional<VnicProfilePoolMember> result = vnicProfilePoolService.getFirstFreeVnicProfileFromPool();

        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(eduVirtVnicProfile2, result.get());

        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId2.toString());
        verify(oVirtVnicProfileService, times(2)).getVnicProfileById(any(String.class));
    }

    @Test
    void Given_NewExistingInOvirtVnicProfile_When_AddVnicProfileToPool_Then_ReturnOptionalVnicProfileFromPool() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(oVirtVnicProfile1);

        when(vnicProfileRepository.saveAndFlush(eduVirtVnicProfile1))
                .thenReturn(eduVirtVnicProfile1);

        VnicProfilePoolMember result = vnicProfilePoolService.addVnicProfileToPool(vnicProfileId1);

        assertNotNull(result);
        assertEquals(eduVirtVnicProfile1, result);

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
    }

    @Test
    void Given_ExistingInOvirtVnicProfileButAlreadyInPool_When_AddVnicProfileToPool_Then_ReturnOptionalVnicProfileFromPool() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        assertThrows(VnicProfileAlreadyExistsException.class,
                () -> vnicProfilePoolService.addVnicProfileToPool(vnicProfileId1)
        );

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(oVirtVnicProfileService, times(0)).getVnicProfileById(vnicProfileId1.toString());
    }

    @Test
    void Given_NewNulledInOvirtVnicProfile_When_AddVnicProfileToPool_Then_ReturnOptionalVnicProfileFromPool() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        when(oVirtVnicProfileService.getVnicProfileById(vnicProfileId1.toString()))
                .thenReturn(null);

        assertThrows(VnicProfileOvirtNotFoundException.class,
                () -> vnicProfilePoolService.addVnicProfileToPool(vnicProfileId1)
        );

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(oVirtVnicProfileService, times(1)).getVnicProfileById(vnicProfileId1.toString());
    }

    @Test
    void Given_ExistingInPoolAndFreeVnicProfile_When_RemoveVnicProfileFromPool_Then_Success() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        doNothing().when(vnicProfileRepository).deleteById(vnicProfileId1);

        vnicProfilePoolService.removeVnicProfileFromPool(vnicProfileId1);

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(1)).deleteById(vnicProfileId1);
    }

    @Test
    void Given_NoExistingInPoolVnicProfile_When_RemoveVnicProfileFromPool_Then_ThrowException() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        assertThrows(VnicProfileEduvirtNotFoundException.class,
                () -> vnicProfilePoolService.removeVnicProfileFromPool(vnicProfileId1)
        );

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(0)).deleteById(vnicProfileId1);
    }

    @Test
    void Given_ExistingInPoolButOccupiedVnicProfile_When_RemoveVnicProfileFromPool_Then_ThrowException() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        eduVirtVnicProfile1.setInUse(true);

        assertThrows(VnicProfileCurrentlyInUseException.class,
                () -> vnicProfilePoolService.removeVnicProfileFromPool(vnicProfileId1)
        );

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(0)).deleteById(vnicProfileId1);
    }

    @Test
    void Given_ExistingInPoolVnicProfile_When_MarkVnicProfileAsOccupied_Then_Success() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        when(vnicProfileRepository.saveAndFlush(eduVirtVnicProfile1))
                .thenReturn(eduVirtVnicProfile1);

        assertFalse(eduVirtVnicProfile1.getInUse());

        vnicProfilePoolService.markVnicProfileAsOccupied(vnicProfileId1);

        assertTrue(eduVirtVnicProfile1.getInUse());

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(1)).saveAndFlush(eduVirtVnicProfile1);
    }

    @Test
    void Given_ExistingInPoolVnicProfile_When_MarkVnicProfileAsOccupied_Then_ThrowException() {
        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        assertFalse(eduVirtVnicProfile1.getInUse());

        assertThrows(VnicProfileEduvirtNotFoundException.class,
                () -> vnicProfilePoolService.markVnicProfileAsOccupied(vnicProfileId1)
        );

        assertFalse(eduVirtVnicProfile1.getInUse());

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(0)).saveAndFlush(any(VnicProfilePoolMember.class));
    }

    @Test
    void Given_ExistingInPoolVnicProfile_When_MarkVnicProfileAsFree_Then_Success() {
        eduVirtVnicProfile1.setInUse(true);

        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.of(eduVirtVnicProfile1));

        when(vnicProfileRepository.saveAndFlush(eduVirtVnicProfile1))
                .thenReturn(eduVirtVnicProfile1);

        assertTrue(eduVirtVnicProfile1.getInUse());

        vnicProfilePoolService.markVnicProfileAsFree(vnicProfileId1);

        assertFalse(eduVirtVnicProfile1.getInUse());

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(1)).saveAndFlush(eduVirtVnicProfile1);
    }

    @Test
    void Given_ExistingInPoolVnicProfile_When_MarkVnicProfileAsFree_Then_ThrowException() {
        eduVirtVnicProfile1.setInUse(true);

        when(vnicProfileRepository.findById(vnicProfileId1))
                .thenReturn(Optional.empty());

        assertTrue(eduVirtVnicProfile1.getInUse());

        assertThrows(VnicProfileEduvirtNotFoundException.class,
                () -> vnicProfilePoolService.markVnicProfileAsFree(vnicProfileId1)
        );

        assertTrue(eduVirtVnicProfile1.getInUse());

        verify(vnicProfileRepository, times(1)).findById(vnicProfileId1);
        verify(vnicProfileRepository, times(0)).saveAndFlush(any(VnicProfilePoolMember.class));
    }

    /* Utils */

    @SneakyThrows
    private void setEntityFieldValue(AbstractEntity entity, String fieldName, Object value) {
        Field idField = AbstractEntity.class.getDeclaredField(fieldName);
        idField.setAccessible(true);
        idField.set(entity, value);
        idField.setAccessible(false);
    }
}
