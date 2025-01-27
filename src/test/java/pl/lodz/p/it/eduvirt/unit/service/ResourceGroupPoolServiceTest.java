package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.exceptions.course.CourseNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group.ResourceGroupPoolNotFoundException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolAlreadyExistsException;
import pl.lodz.p.it.eduvirt.exceptions.resource_group_pool.ResourceGroupPoolConflictException;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.service.impl.ResourceGroupPoolServiceImpl;
import pl.lodz.p.it.eduvirt.service.priviliges.PrivilegesService;
import pl.lodz.p.it.eduvirt.util.etag.ETagHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResourceGroupPoolServiceTest {
    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private PrivilegesService privilegesService;

    @Mock
    private ETagHelper eTagHelper;

    @InjectMocks
    private ResourceGroupPoolServiceImpl sut;

    @Test
    void Given_UserIsAssignedToCourse_When_AddResourceGroupPool_Then_ResourceGroupPoolIsAdded() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        Course course = Course.builder()
                .resourceGroupPools(List.of())
                .build();
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(course));
        when(resourceGroupPoolRepository.save(any())).thenReturn(pool);
        // When
        sut.addResourceGroupPool(pool, id);
        // Then
        verify(resourceGroupPoolRepository, times(1))
                .save(any());
        assertEquals(pool.getCourse(), course);
    }

    @Test
    void Given_UserIsNotAssignedToCourse_When_AddResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        when(privilegesService.validateCourseOwnership(any())).thenReturn(false);
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(new Course()));
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.addResourceGroupPool(pool, id));
    }

    @Test
    void Given_ResourceGroupPoolNameIsTaken_When_AddResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().name("name").build();
        Course course = Course.builder()
                .resourceGroupPools(List.of(pool))
                .build();
        when(courseRepository.findById(any()))
                .thenReturn(Optional.of(course));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        // When
        // Then
        assertThrows(ResourceGroupPoolAlreadyExistsException.class, () -> sut.addResourceGroupPool(pool, id));
    }

    @Test
    void Given_UserExistsAndIsAdminOrTeacherInCourse_When_GetResourceGroupPoolsByCourse_Then_ReturnResourceGroupPools() {
        // Given
        UUID id = UUID.randomUUID();
        Course course = Course.builder()
                .resourceGroupPools(List.of(ResourceGroupPool.builder().build()))
                .build();
        when(privilegesService.validateCourseOwnershipOrAdmin(any())).thenReturn(true);
        when(courseRepository.findById(any())).thenReturn(Optional.of(course));
        // When
        List<ResourceGroupPool> result = sut.getResourceGroupPoolsByCourse(id);
        // Then
        assertEquals(course.getResourceGroupPools(), result);
    }

    @Test
    void Given_UserIsNotAdminOrTeacherInCourse_When_GetResourceGroupPoolsByCourse_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(courseRepository.findById(any())).thenReturn(Optional.of(new Course()));
        when(privilegesService.validateCourseOwnershipOrAdmin(any())).thenReturn(false);
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.getResourceGroupPoolsByCourse(id));
    }

    @Test
    void Given_ResourceGroupPoolExists_When_GetResourceGroupPool_Then_ReturnResourceGroupPool() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        // When
        ResourceGroupPool result = sut.getResourceGroupPool(id);
        // Then
        assertEquals(pool, result);
    }

    @Test
    void Given_ResourceGroupPoolDoesNotExist_When_GetResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupPoolNotFoundException.class, () -> sut.getResourceGroupPool(id));
    }

    @Test
    void Given_UserIsAssignedToCourse_When_AddResourceGroupToPool_Then_ResourceGroupIsAddedToPool() {
        // Given
        UUID poolId = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .resourceGroups(new ArrayList<>())
                .description("description")
                .maxRentTime(1)
                .build();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        when(resourceGroupPoolRepository.save(any())).thenReturn(pool);
        // When
        sut.addResourceGroupToPool(poolId, resourceGroup);
        // Then
        verify(resourceGroupPoolRepository, times(1)).save(any());
        assertEquals(1, pool.getResourceGroups().size());
        assertEquals(resourceGroup, pool.getResourceGroups().getFirst());
        assertEquals(pool.getDescription(), resourceGroup.getDescription());
        assertEquals(pool.getMaxRentTime(), resourceGroup.getMaxRentTime());
        assertTrue(resourceGroup.isStateless());
    }

    @Test
    void Given_ResourceGroupPoolNotExists_When_AddResourceGroupToPool_Then_ExceptionIsThrown() {
        // Given
        UUID poolId = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupPoolNotFoundException.class, () -> sut.addResourceGroupToPool(poolId, resourceGroup));
    }

    @Test
    void Given_UserIsNotAssignedToCourse_When_AddResourceGroupToPool_Then_ExceptionIsThrown() {
        // Given
        UUID poolId = UUID.randomUUID();
        ResourceGroup resourceGroup = ResourceGroup.builder().build();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .course(new Course())
                .build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(false);
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.addResourceGroupToPool(poolId, resourceGroup));
    }

    @Test
    void Given_UserIsAssignedToCourse_When_DeleteResourceGroupPool_Then_ResourceGroupPoolIsDeleted() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        // When
        sut.deleteResourceGroupPool(id);
        // Then
        verify(resourceGroupPoolRepository, times(1)).delete(pool);
    }

    @Test
    void Given_UserIsNotAssignedToCourse_When_DeleteResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .course(new Course())
                .build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(false);
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.deleteResourceGroupPool(id));
    }

    @Test
    void Given_ResourceGroupPoolDoNotExists_When_DeleteResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupPoolNotFoundException.class, () -> sut.deleteResourceGroupPool(id));
    }

    @Test
    void Given_ResourceGroupPoolDoNotExists_When_UpdateResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.empty());
        // When
        // Then
        assertThrows(ResourceGroupPoolNotFoundException.class, () -> sut.updateResourceGroupPool(id, pool, ""));
    }

    @Test
    void Given_UserIsNotAssignedToCourse_When_UpdateResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .course(new Course())
                .build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(false);
        // When
        // Then
        assertThrows(CourseNotFoundException.class, () -> sut.updateResourceGroupPool(id, pool, ""));
    }

    @Test
    void Given_ETagValidationFailed_When_UpdateResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder().build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        when(eTagHelper.validateEtag(any(), any())).thenReturn(false);
        // When
        // Then
        assertThrows(ResourceGroupPoolConflictException.class, () -> sut.updateResourceGroupPool(id, pool, "etag"));
    }

    @Test
    void Given_NameIsAlreadyTakenByOtherResourceGroup_When_UpdateResourceGroupPool_Then_ExceptionIsThrown() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .course(new Course())
                .name("name")
                .build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);
        when(resourceGroupPoolRepository.existsByCourseIdAndNameAndIdNot(any(), any(), any())).thenReturn(true);
        // When
        // Then
        assertThrows(ResourceGroupPoolAlreadyExistsException.class, () -> sut.updateResourceGroupPool(id, pool, "etag"));
    }

    @Test
    void Given_InputDataIsCorrect_When_UpdateResourceGroupPool_Then_ResourceGroupPoolIsUpdated() {
        // Given
        UUID id = UUID.randomUUID();
        ResourceGroupPool pool = ResourceGroupPool.builder()
                .course(new Course())
                .name("old_name")
                .description("old_description")
                .maxRentTime(1)
                .gracePeriod(1)
                .maxRent(1)
                .resourceGroups(List.of(
                        ResourceGroup.builder()
                                .description("old_description")
                                .maxRentTime(1)
                                .build(),
                        ResourceGroup.builder()
                                .description("old_description2")
                                .maxRentTime(3)
                                .build()
                ))
                .build();

        ResourceGroupPool updatedPool = ResourceGroupPool.builder()
                .name("new_name")
                .description("new_description")
                .maxRentTime(2)
                .gracePeriod(2)
                .maxRent(2)
                .build();
        when(resourceGroupPoolRepository.findById(any())).thenReturn(Optional.of(pool));
        when(privilegesService.validateCourseOwnership(any())).thenReturn(true);
        when(eTagHelper.validateEtag(any(), any())).thenReturn(true);
        when(resourceGroupPoolRepository.existsByCourseIdAndNameAndIdNot(any(), any(), any())).thenReturn(false);
        when(resourceGroupPoolRepository.save(any())).thenReturn(pool);
        // When
        sut.updateResourceGroupPool(id, updatedPool, "etag");
        // Then
        verify(resourceGroupPoolRepository, times(1)).save(pool);
        assertEquals(updatedPool.getName(), pool.getName());
        assertEquals(updatedPool.getDescription(), pool.getDescription());
        assertEquals(updatedPool.getMaxRentTime(), pool.getMaxRentTime());
        assertEquals(updatedPool.getGracePeriod(), pool.getGracePeriod());
        assertEquals(updatedPool.getMaxRent(), pool.getMaxRent());
        pool.getResourceGroups().forEach(rg -> {
            assertEquals(updatedPool.getDescription(), rg.getDescription());
            assertEquals(updatedPool.getMaxRentTime(), rg.getMaxRentTime());
        });
    }
}
