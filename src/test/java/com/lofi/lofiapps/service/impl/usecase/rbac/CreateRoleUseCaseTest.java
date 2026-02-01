package com.lofi.lofiapps.service.impl.usecase.rbac;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.CreateRoleRequest;
import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.repository.PermissionRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateRoleUseCaseTest {

  @Mock private RoleRepository roleRepository;
  @Mock private PermissionRepository permissionRepository;

  @InjectMocks private CreateRoleUseCase createRoleUseCase;

  private UUID roleId;
  private UUID permissionId;
  private CreateRoleRequest request;
  private Role savedRole;
  private Permission permission;

  @BeforeEach
  void setUp() {
    roleId = UUID.randomUUID();
    permissionId = UUID.randomUUID();

    request =
        CreateRoleRequest.builder()
            .name("ROLE_TEST")
            .description("Test Role")
            .permissionIds(List.of(permissionId))
            .build();

    permission =
        Permission.builder()
            .id(permissionId)
            .name("TEST_PERMISSION")
            .description("Test Permission")
            .build();

    savedRole =
        Role.builder()
            .id(roleId)
            .name("ROLE_TEST")
            .description("Test Role")
            .permissions(java.util.Set.of(permission))
            .build();
  }

  @Test
  @DisplayName("Execute should create role successfully with permissions")
  void execute_ShouldCreateRoleSuccessfully() {
    // Arrange
    when(roleRepository.findByName("ROLE_TEST")).thenReturn(Optional.empty());
    when(permissionRepository.findAllById(List.of(permissionId))).thenReturn(List.of(permission));
    when(roleRepository.save(any(Role.class))).thenReturn(savedRole);

    // Act
    RoleResponse result = createRoleUseCase.execute(request);

    // Assert
    assertNotNull(result);
    assertEquals(roleId, result.getId());
    assertEquals("ROLE_TEST", result.getName());
    assertEquals("Test Role", result.getDescription());
    assertNotNull(result.getPermissions());
    assertEquals(1, result.getPermissions().size());
    assertEquals("TEST_PERMISSION", result.getPermissions().get(0).getName());
    verify(roleRepository).findByName("ROLE_TEST");
    verify(roleRepository).save(any(Role.class));
  }

  @Test
  @DisplayName("Execute should create role without permissions")
  void execute_ShouldCreateRoleWithoutPermissions() {
    // Arrange
    CreateRoleRequest requestWithoutPermissions =
        CreateRoleRequest.builder().name("ROLE_SIMPLE").description("Simple Role").build();

    Role savedRoleWithoutPermissions =
        Role.builder().id(UUID.randomUUID()).name("ROLE_SIMPLE").description("Simple Role").build();

    when(roleRepository.findByName("ROLE_SIMPLE")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(savedRoleWithoutPermissions);

    // Act
    RoleResponse result = createRoleUseCase.execute(requestWithoutPermissions);

    // Assert
    assertNotNull(result);
    assertEquals("ROLE_SIMPLE", result.getName());
    assertNotNull(result.getPermissions());
    assertTrue(result.getPermissions().isEmpty());
  }

  @Test
  @DisplayName("Execute should throw exception when role already exists")
  void execute_ShouldThrowException_WhenRoleExists() {
    // Arrange
    when(roleRepository.findByName("ROLE_TEST")).thenReturn(Optional.of(savedRole));

    // Act & Assert
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> createRoleUseCase.execute(request));
    assertEquals("Role already exists: ROLE_TEST", exception.getMessage());
    verify(roleRepository).findByName("ROLE_TEST");
    verify(roleRepository, never()).save(any(Role.class));
  }

  @Test
  @DisplayName("Execute should handle empty permission list")
  void execute_ShouldHandleEmptyPermissionList() {
    // Arrange
    CreateRoleRequest requestWithEmptyPermissions =
        CreateRoleRequest.builder()
            .name("ROLE_EMPTY")
            .description("Empty Role")
            .permissionIds(Collections.emptyList())
            .build();

    Role savedRoleWithEmptyPermissions =
        Role.builder().id(UUID.randomUUID()).name("ROLE_EMPTY").description("Empty Role").build();

    when(roleRepository.findByName("ROLE_EMPTY")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenReturn(savedRoleWithEmptyPermissions);

    // Act
    RoleResponse result = createRoleUseCase.execute(requestWithEmptyPermissions);

    // Assert
    assertNotNull(result);
    assertEquals("ROLE_EMPTY", result.getName());
    assertTrue(result.getPermissions().isEmpty());
  }
}
