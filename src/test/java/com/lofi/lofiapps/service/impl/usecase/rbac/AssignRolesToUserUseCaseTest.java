package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.AssignRolesRequest;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssignRolesToUserUseCaseTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;

  @InjectMocks private AssignRolesToUserUseCase assignRolesToUserUseCase;

  private UUID userId;
  private UUID roleId;
  private User user;
  private Role role;
  private AssignRolesRequest request;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    roleId = UUID.randomUUID();

    user =
        User.builder()
            .id(userId)
            .email("user@example.com")
            .username("testuser")
            .roles(new HashSet<>())
            .build();

    role = Role.builder().id(roleId).name(RoleName.ROLE_MARKETING).description("Marketing").build();

    request = AssignRolesRequest.builder().roleIds(List.of(roleId)).build();
  }

  @Test
  @DisplayName("Execute should assign roles to user successfully")
  void execute_ShouldAssignRolesSuccessfully() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findAllById(List.of(roleId))).thenReturn(List.of(role));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    assignRolesToUserUseCase.execute(userId, request);

    // Assert
    assertTrue(user.getRoles().contains(role));
    verify(userRepository).findById(userId);
    verify(roleRepository).findAllById(List.of(roleId));
    verify(userRepository).save(user);
  }

  @Test
  @DisplayName("Execute should throw exception when user not found")
  void execute_ShouldThrowException_WhenUserNotFound() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> assignRolesToUserUseCase.execute(userId, request));
    assertEquals("User", exception.getResourceName());
    verify(userRepository).findById(userId);
    verify(roleRepository, never()).findAllById(any());
  }

  @Test
  @DisplayName("Execute should throw exception when user is a customer")
  void execute_ShouldThrowException_WhenUserIsCustomer() {
    // Arrange
    Role customerRole = Role.builder().id(UUID.randomUUID()).name(RoleName.ROLE_CUSTOMER).build();
    user.setRoles(new HashSet<>(Set.of(customerRole)));

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> assignRolesToUserUseCase.execute(userId, request));
    assertEquals(
        "Customer roles cannot be managed through this administrative API", exception.getMessage());
    verify(userRepository).findById(userId);
    verify(roleRepository, never()).findAllById(any());
  }

  @Test
  @DisplayName("Execute should throw exception when some roles not found")
  void execute_ShouldThrowException_WhenRolesNotFound() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findAllById(List.of(roleId))).thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> assignRolesToUserUseCase.execute(userId, request));
    assertEquals("Role", exception.getResourceName());
    verify(userRepository).findById(userId);
    verify(roleRepository).findAllById(List.of(roleId));
  }

  @Test
  @DisplayName("Execute should throw exception when trying to assign CUSTOMER role")
  void execute_ShouldThrowException_WhenAssigningCustomerRole() {
    // Arrange
    Role customerRole =
        Role.builder().id(roleId).name(RoleName.ROLE_CUSTOMER).description("Customer").build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findAllById(List.of(roleId))).thenReturn(List.of(customerRole));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> assignRolesToUserUseCase.execute(userId, request));
    assertEquals("Cannot manually assign CUSTOMER role", exception.getMessage());
  }

  @Test
  @DisplayName("Execute should handle null roles set")
  void execute_ShouldHandleNullRolesSet() {
    // Arrange
    user.setRoles(null);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findAllById(List.of(roleId))).thenReturn(List.of(role));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    assignRolesToUserUseCase.execute(userId, request);

    // Assert
    assertNotNull(user.getRoles());
    assertTrue(user.getRoles().contains(role));
  }

  @Test
  @DisplayName("Execute should assign multiple roles")
  void execute_ShouldAssignMultipleRoles() {
    // Arrange
    UUID roleId2 = UUID.randomUUID();
    Role role2 =
        Role.builder()
            .id(roleId2)
            .name(RoleName.ROLE_BACKOFFICE)
            .description("Back Office")
            .build();

    AssignRolesRequest multiRoleRequest =
        AssignRolesRequest.builder().roleIds(List.of(roleId, roleId2)).build();

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(roleRepository.findAllById(List.of(roleId, roleId2))).thenReturn(List.of(role, role2));
    when(userRepository.save(any(User.class))).thenReturn(user);

    // Act
    assignRolesToUserUseCase.execute(userId, multiRoleRequest);

    // Assert
    assertEquals(2, user.getRoles().size());
    assertTrue(user.getRoles().contains(role));
    assertTrue(user.getRoles().contains(role2));
  }
}
