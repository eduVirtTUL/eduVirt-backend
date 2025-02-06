package pl.lodz.p.it.eduvirt.unit.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.entity.AbstractEntity;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupAlreadyExists;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupConflictException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupNotFoundException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupRepository;
import pl.lodz.p.it.eduvirt.service.impl.ResourceGroupServiceImpl;
import pl.lodz.p.it.eduvirt.service.ovirt.OVirtVmService;
import pl.lodz.p.it.eduvirt.service.privileges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceGroupServiceTest {
    @Mock
    private ETagHelper eTagHelper;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PrivilegesService privilegesService;

    @Mock
    private ResourceGroupRepository resourceGroupRepository;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private OVirtVmService oVirtVmService;

    @InjectMocks
    private ResourceGroupServiceImpl sut;

    @Test
    void Given_ResourceGroupsExists_When_GetAll_Then_ReturnAllResourceGroups() {
        List<ResourceGroup> resourceGroups = List.of(
                new ResourceGroup(),
                new ResourceGroup()
        );

        when(resourceGroupRepository.findAll()).thenReturn(resourceGroups);

        var result = sut.getResourceGroups();
        assertEquals(resourceGroups, result);
        assertEquals(2, result.size());
    }

    @Test
    void Given_NoResourceGroupsExists_When_GetAll_Then_ReturnEmptyList() {
        List<ResourceGroup> resourceGroups = List.of();

        when(resourceGroupRepository.findAll()).thenReturn(resourceGroups);

        var result = sut.getResourceGroups();
        assertEquals(resourceGroups, result);
        assertEquals(0, result.size());
    }

    @Test
    void Given_ResourceGroupExists_When_GetById_Then_ReturnResourceGroup() {
        UUID id = UUID.randomUUID();

        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(privilegesService.validateResourceGroupOwnershipOrAdminOrStudentWithAccess(resourceGroup))
                .thenReturn(true);

        var result = sut.getResourceGroup(id);
        assertEquals(resourceGroup, result);
    }

    @Test
    void Given_NoResourceGroupExists_When_GetById_Then_ThrowException() {
        UUID id = UUID.randomUUID();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.getResourceGroup(id));
    }

    @Test
    void Given_ResourceGroupDoesNotExist_When_Delete_Then_ThrowException() {
        UUID id = UUID.randomUUID();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteResourceGroup(id));
    }

    @Test
    void Given_StatelessResourceGroupExistsAndUserIsOwner_When_Delete_Then_DeleteResourceGroup() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .stateless(true)
                .build();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .resourceGroups(new ArrayList<>(List.of(resourceGroup)))
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup))
                .thenReturn(pool);

        sut.deleteResourceGroup(id);
    }

    @Test
    void Given_ResourceGroupExistsAndUserIsNotOwner_When_Delete_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(false);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.deleteResourceGroup(id));
    }

    @Test
    void Given_StatefulResourceGroupExistsAndUserIsOwner_When_Delete_Then_DeleteResourceGroup() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .stateless(false)
                .build();

        Course course = Course.builder()
                .stateFulResourceGroups(new ArrayList<>(List.of(resourceGroup)))
                .build();

        when(courseRepository.findByStateFulResourceGroupsContaining(resourceGroup))
                .thenReturn(course);

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        sut.deleteResourceGroup(id);

        assertEquals(0, course.getStateFulResourceGroups().size());
    }

    @Test
    void Given_ResourceGroupNotExists_When_Update_Then_ThrowException() {
        UUID id = UUID.randomUUID();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.updateResourceGroup(id, new ResourceGroup(), ""));
    }

    @Test
    void Given_ResourceGroupExistsAndUserIsNotOwner_When_Update_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(false);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.updateResourceGroup(id, new ResourceGroup(), ""));
    }

    @Test
    void Given_ResourceGroupETagNotMatch_When_Update_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(eTagHelper.validateEtag(any(), any())).thenReturn(false);

        assertThrows(ResourceGroupConflictException.class, () -> sut.updateResourceGroup(id, new ResourceGroup(), ""));
    }

    @Test
    void Given_StatelessResourceGroupExistsAndUserIsOwner_When_Update_Then_UpdateResourceGroup() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .description("Description")
                .maxRentTime(10)
                .stateless(true)
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);

        when(resourceGroupRepository.save(any())).thenReturn(resourceGroup);
        when(resourceGroupPoolRepository.findByResourceGroupsContaining(resourceGroup))
                .thenReturn(new ResourceGroupPool());

        ResourceGroup updatedResourceGroup = ResourceGroup.builder()
                .name("UpdatedResourceGroup")
                .description("UpdatedDescription")
                .maxRentTime(20)
                .build();

        sut.updateResourceGroup(id, updatedResourceGroup, "");

        assertEquals(updatedResourceGroup.getName(), resourceGroup.getName());
        assertNotEquals(updatedResourceGroup.getDescription(), resourceGroup.getDescription());
        assertNotEquals(updatedResourceGroup.getMaxRentTime(), resourceGroup.getMaxRentTime());
    }

    @Test
    void Given_StatelessResourceGroupWithNameAlreadyExists_When_Update_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = getResourceGroupWithRandomId();
        resourceGroup.setStateless(true);
        resourceGroup.setName("ResourceGroup");

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);

        ResourceGroup updatedResourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        ResourceGroup resourceGroup2 = getResourceGroupWithRandomId();
        resourceGroup2.setStateless(true);
        resourceGroup2.setName("ResourceGroup");

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(any()))
                .thenReturn(
                        ResourceGroupPool.builder()
                                .resourceGroups(List.of(resourceGroup2))
                                .build()
                );

        assertThrows(ResourceGroupAlreadyExists.class, () -> sut.updateResourceGroup(id, updatedResourceGroup, ""));
    }

    @Test
    void Given_StatefulResourceGroupExistsAndUserIsOwner_When_Update_Then_UpdateResourceGroup() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .description("Description")
                .maxRentTime(10)
                .stateless(false)
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);

        when(resourceGroupRepository.save(any())).thenReturn(resourceGroup);

        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(Course.builder()
                        .stateFulResourceGroups(List.of())
                        .build());

        ResourceGroup updatedResourceGroup = ResourceGroup.builder()
                .name("UpdatedResourceGroup")
                .description("UpdatedDescription")
                .maxRentTime(20)
                .build();

        sut.updateResourceGroup(id, updatedResourceGroup, "");

        assertEquals(updatedResourceGroup.getName(), resourceGroup.getName());
        assertEquals(updatedResourceGroup.getDescription(), resourceGroup.getDescription());
        assertEquals(updatedResourceGroup.getMaxRentTime(), resourceGroup.getMaxRentTime());
    }

    @Test
    void Given_StatefulResourceGroupWithNameAlreadyExists_When_Update_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = getResourceGroupWithRandomId();
        resourceGroup.setStateless(false);
        resourceGroup.setName("ResourceGroup");

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);

        ResourceGroup updatedResourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        ResourceGroup resourceGroup2 = getResourceGroupWithRandomId();
        resourceGroup2.setStateless(false);
        resourceGroup2.setName("ResourceGroup");

        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(Course.builder()
                        .stateFulResourceGroups(List.of(resourceGroup2))
                        .build());

        assertThrows(ResourceGroupAlreadyExists.class, () -> sut.updateResourceGroup(id, updatedResourceGroup, ""));
    }

    @Test
    void Given_ResourceGroupDoNotExists_When_findAvailableVms_Then_ThrowException() {
        UUID id = UUID.randomUUID();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.findAvailableVms(id));
    }

    @Test
    void Given_ResourceGroupExistsAndUserIsNotOwner_When_findAvailableVms_Then_ThrowException() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(false);

        UUID id = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .build();

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(resourceGroup));

        assertThrows(ResourceGroupNotFoundException.class, () -> sut.findAvailableVms(id));
    }

    @Test
    void Given_StatefulResourceGroupExistsAndUserIsOwner_When_findAvailableVms_Then_ReturnAvailableVms() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup stateFulResourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .stateless(false)
                .build();

        when(courseRepository.findByStateFulResourceGroupsContaining(any()))
                .thenReturn(Course.builder()
                        .clusterId(UUID.randomUUID())
                        .build());

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(stateFulResourceGroup));

        when(oVirtVmService.findVms())
                .thenReturn(List.of());

        var result = sut.findAvailableVms(id);
        assertEquals(0, result.size());
    }

    @Test
    void Given_StatelessResourceGroupExistsAndUserIsOwner_When_findAvailableVms_Then_ReturnAvailableVms() {
        when(privilegesService.validateResourceGroupOwnership(any())).thenReturn(true);

        UUID id = UUID.randomUUID();
        ResourceGroup stateLessResourceGroup = ResourceGroup.builder()
                .name("ResourceGroup")
                .stateless(true)
                .build();

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(any()))
                .thenReturn(ResourceGroupPool.builder()
                        .course(Course.builder()
                                .clusterId(UUID.randomUUID())
                                .build())
                        .build());

        when(resourceGroupRepository.findById(id))
                .thenReturn(Optional.of(stateLessResourceGroup));

        when(oVirtVmService.findVms())
                .thenReturn(List.of());

        var result = sut.findAvailableVms(id);
        assertEquals(0, result.size());
    }

    @SneakyThrows
    private ResourceGroup getResourceGroupWithRandomId() {
        ResourceGroup build = ResourceGroup.builder()
                .build();

        Field idField = AbstractEntity.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(build, UUID.randomUUID());
        idField.setAccessible(false);


        return build;
    }
}
