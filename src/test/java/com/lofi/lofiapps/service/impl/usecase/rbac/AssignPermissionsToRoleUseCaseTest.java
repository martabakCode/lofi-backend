package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.AssignPermissionsRequest;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.PermissionRepository;
import com.lofi.lofiapps.repository.RoleRepository;
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
class AssignPermissionsToRoleUseCaseTest {

  @Mock private RoleRepository roleRepository;
  @Mock private PermissionRepository permissionRepository;

  @InjectMocks private AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;

  private UUID roleId;
  private UUID permissionId;
  private Role role;
  private Permission permission;
  private AssignPermissionsRequest request;

  @BeforeEach
  void setUp() {
    roleId = UUID.randomUUID();
    permissionId = UUID.randomUUID();

    role = Role.builder().id(roleId).name(RoleName.ROLE_TEST).permissions(new HashSet<>()).build();

    permission =
        Permission.builder()
            .id(permissionId)
            .name("TEST_PERMISSION")
            .description("Test Permission")
            .build();

    request = AssignPermissionsRequest.builder().permissionIds(List.of(permissionId)).build();
  }

  @Test
  @DisplayName("Execute should assign permissions to role successfully")
  void execute_ShouldAssignPermissionsSuccessfully() {
    // Arrange
    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
    when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));
    when(roleRepository.save(any(Role.class))).thenReturn(role);

    // Act
    assignPermissionsToRoleUseCase.execute(roleId, request);

    // Assert
    assertTrue(role.getPermissions().contains(permission));
    verify(roleRepository).findById(roleId);
    verify(permissionRepository).findAllById(List.of(permissionId));
    verify(roleRepository).save(role);
  }

  @Test
  @DisplayName("Execute should throw exception when role not found")
  void execute_ShouldThrowException_WhenRoleNotFound() {
    // Arrange
    when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> assignPermissionsToRoleUseCase.execute(roleId, request));
    assertEquals("Role", exception.getResourceName());
    verify(roleRepository).findById(roleId);
    verify(permissionRepository, never()).findAllById(any());
  }

  @Test
  @DisplayName("Execute should throw exception when some permissions not found")
  void execute_ShouldThrowException_WhenPermissionsNotFound() {
    // Arrange
    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
    when(permissionRepository.findAllById(List.of(permissionId)))
        .thenReturn(Collections.emptyList());

    // Act & Assert
    ResourceNotFoundException exception =
        assertThrows(
            ResourceNotFoundException.class,
            () -> assignPermissionsToRoleUseCase.execute(roleId, request));
    assertEquals("Permission", exception.getResourceName());
    verify(roleRepository).findById(roleId);
    verify(permissionRepository).findAllById(List.of(permissionId));
  }

  @Test
  @DisplayName("Execute should handle null permissions set")
  void execute_ShouldHandleNullPermissionsSet() {
    // Arrange
    role.setPermissions(null);

    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
    when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));
    when(roleRepository.save(any(Role.class))).thenReturn(role);

    // Act
    assignPermissionsToRoleUseCase.execute(roleId, request);

    // Assert
    assertNotNull(role.getPermissions());
    assertTrue(role.getPermissions().contains(permission));
  }

  @Test
  @DisplayName("Execute should assign multiple permissions")
  void execute_ShouldAssignMultiplePermissions() {
    // Arrange
    UUID permissionId2 = UUID.randomUUID();
    Permission permission2 =
        Permission.builder()
            .id(permissionId2)
            .name("ANOTHER_PERMISSION")
            .description("Another Permission")
            .build();

    AssignPermissionsRequest multiPermissionRequest =
        AssignPermissionsRequest.builder()
            .permissionIds(List.of(permissionId, permissionId2))
            .build();

    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
    when(permissionRepository.findAllById(List.of(permissionId, permissionId2)))
        .thenReturn(List.of(permission, permission2));
    when(roleRepository.save(any(Role.class))).thenReturn(role);

    // Act
    assignPermissionsToRoleUseCase.execute(roleId, multiPermissionRequest);

    // Assert
    assertEquals(2, role.getPermissions().size());
    assertTrue(role.getPermissions().contains(permission));
    assertTrue(role.getPermissions().contains(permission2));
  }

  @Test
  @DisplayName("Execute should add permissions to existing ones")
  void execute_ShouldAddToExistingPermissions() {
    // Arrange
    UUID existingPermissionId = UUID.randomUUID();
    Permission existingPermission =
        Permission.builder()
            .id(existingPermissionId)
            .name("EXISTING_PERMISSION")
            .description("Existing Permission")
            .build();
    role.setPermissions(new HashSet<>(Set.of(existingPermission)));

    when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
    when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));
    when(roleRepository.save(any(Role.class))).thenReturn(role);

    // Act
    assignPermissionsToRoleUseCase.execute(roleId, request);

    // Assert
    assertEquals(2, role.getPermissions().size());
    assertTrue(role.getPermissions().contains(existingPermission));
    assertTrue(role.getPermissions().contains(permission));
  }
}
