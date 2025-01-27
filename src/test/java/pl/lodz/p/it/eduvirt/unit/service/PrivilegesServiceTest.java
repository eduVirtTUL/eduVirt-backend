package pl.lodz.p.it.eduvirt.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.lodz.p.it.eduvirt.entity.Course;
import pl.lodz.p.it.eduvirt.entity.ResourceGroup;
import pl.lodz.p.it.eduvirt.entity.ResourceGroupPool;
import pl.lodz.p.it.eduvirt.entity.User;
import pl.lodz.p.it.eduvirt.repository.CourseRepository;
import pl.lodz.p.it.eduvirt.repository.ResourceGroupPoolRepository;
import pl.lodz.p.it.eduvirt.repository.UserRepository;
import pl.lodz.p.it.eduvirt.service.priviliges.impl.PrivilegesServiceImpl;
import pl.lodz.p.it.eduvirt.util.RoleConstants;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivilegesServiceTest {
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private ResourceGroupPoolRepository resourceGroupPoolRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrivilegesServiceImpl sut;

    @BeforeEach
    void setUp() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(UUID.randomUUID().toString());
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void Given_UserIsOwner_When_ValidateResourceGroupOwnership_Then_ReturnTrue() {
        // Given
        ResourceGroup stateless = ResourceGroup.builder()
                .stateless(true)
                .build();

        ResourceGroup stateful = ResourceGroup.builder()
                .stateless(false)
                .build();

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(stateless))
                .thenReturn(
                        ResourceGroupPool.builder()
                                .course(Course.builder().build())
                                .build()
                );

        when(courseRepository.existsCourseForTeacher(any(), any()))
                .thenReturn(true);

        when(courseRepository.findByStateFulResourceGroupsContaining(stateful))
                .thenReturn(Course.builder().build());

        // When
        boolean result1 = sut.validateResourceGroupOwnership(stateless);
        boolean result2 = sut.validateResourceGroupOwnership(stateful);
        // Then
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void Given_UserIsOwner_When_ValidateResourceGroupOwnershipOrAdmin_Then_ReturnTrue() {
        // Given
        ResourceGroup stateless = ResourceGroup.builder()
                .stateless(true)
                .build();

        ResourceGroup stateful = ResourceGroup.builder()
                .stateless(false)
                .build();

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(stateless))
                .thenReturn(
                        ResourceGroupPool.builder()
                                .course(Course.builder().build())
                                .build()
                );

        when(courseRepository.existsCourseForTeacher(any(), any()))
                .thenReturn(true);

        when(courseRepository.findByStateFulResourceGroupsContaining(stateful))
                .thenReturn(Course.builder().build());

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(User.builder()
                        .roles(List.of(RoleConstants.STUDENT))
                        .build()));

        // When
        boolean result1 = sut.validateResourceGroupOwnershipOrAdmin(stateless);
        boolean result2 = sut.validateResourceGroupOwnershipOrAdmin(stateful);
        // Then
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void Given_UserIsAdmin_When_ValidateResourceGroupOwnershipOrAdmin_Then_ReturnTrue() {
        // Given
        ResourceGroup stateless = ResourceGroup.builder()
                .stateless(true)
                .build();

        ResourceGroup stateful = ResourceGroup.builder()
                .stateless(false)
                .build();

        when(resourceGroupPoolRepository.findByResourceGroupsContaining(stateless))
                .thenReturn(
                        ResourceGroupPool.builder()
                                .course(Course.builder().build())
                                .build()
                );

        when(courseRepository.existsCourseForTeacher(any(), any()))
                .thenReturn(false);

        when(courseRepository.findByStateFulResourceGroupsContaining(stateful))
                .thenReturn(Course.builder().build());

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(User.builder()
                        .roles(List.of(RoleConstants.ADMINISTRATOR))
                        .build()));

        // When
        boolean result1 = sut.validateResourceGroupOwnershipOrAdmin(stateless);
        boolean result2 = sut.validateResourceGroupOwnershipOrAdmin(stateful);
        // Then
        assertTrue(result1);
        assertTrue(result2);
    }

    @Test
    void Given_UserIsOwner_When_ValidateCourseOwnership_Then_ReturnTrue() {
        // Given
        Course course = Course.builder().build();

        when(courseRepository.existsCourseForTeacher(any(), any()))
                .thenReturn(true);

        // When
        boolean result = sut.validateCourseOwnership(course);
        // Then
        assertTrue(result);
    }

    @Test
    void Given_UserIsOwner_When_ValidateCourseOwnershipOrAdmin_Then_ReturnTrue() {
        // Given
        Course course = Course.builder().build();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(User.builder()
                        .roles(List.of(RoleConstants.TEACHER))
                        .build()));

        when(courseRepository.existsCourseForTeacher(any(), any())).thenReturn(true);

        // When
        boolean result = sut.validateCourseOwnershipOrAdmin(course);
        // Then
        assertTrue(result);
    }

    @Test
    void Given_UserIsAdmin_When_ValidateCourseOwnershipOrAdmin_Then_ReturnTrue() {
        // Given
        Course course = Course.builder().build();

        when(userRepository.findById(any()))
                .thenReturn(Optional.of(User.builder()
                        .roles(List.of(RoleConstants.ADMINISTRATOR))
                        .build()));

        // When
        boolean result = sut.validateCourseOwnershipOrAdmin(course);
        // Then
        assertTrue(result);
        verify(courseRepository, times(0)).existsCourseForTeacher(any(), any());
    }

    @Test
    void Given_UserIsNotAdminNotOwnerInCourse_When_ValidateCourseOwnershipOrAdmin_Then_ReturnFalse() {
        // Given
        Course course = Course.builder().build();
        when(userRepository.findById(any()))
                .thenReturn(Optional.of(User.builder()
                        .roles(List.of(RoleConstants.STUDENT))
                        .build()));
        when(courseRepository.existsCourseForTeacher(any(), any())).thenReturn(false);
        // When
        boolean result = sut.validateCourseOwnershipOrAdmin(course);
        // Then
        assertFalse(result);
    }

    @Test
    void Given_UserIsNotOwnerInCourse_When_ValidateCourseOwnership_Then_ReturnFalse() {
        // Given
        Course course = Course.builder().build();
        when(courseRepository.existsCourseForTeacher(any(), any())).thenReturn(false);
        // When
        boolean result = sut.validateCourseOwnership(course);
        // Then
        assertFalse(result);
    }
}
