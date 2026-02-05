package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.service.impl.usecase.rbac.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RbacServiceImplTest {

  @Mock private GetRolesUseCase getRolesUseCase;
  @Mock private CreateRoleUseCase createRoleUseCase;
  @Mock private UpdateRoleUseCase updateRoleUseCase;
  @Mock private DeleteRoleUseCase deleteRoleUseCase;
  @Mock private GetPermissionsUseCase getPermissionsUseCase;
  @Mock private GetRolePermissionsUseCase getRolePermissionsUseCase;
  @Mock private AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;
  @Mock private RemovePermissionFromRoleUseCase removePermissionFromRoleUseCase;
  @Mock private GetUserRolesUseCase getUserRolesUseCase;
  @Mock private AssignRolesToUserUseCase assignRolesToUserUseCase;
  @Mock private RemoveRoleFromUserUseCase removeRoleFromUserUseCase;
  @Mock private GetBranchesUseCase getBranchesUseCase;
  @Mock private CreateBranchUseCase createBranchUseCase;
  @Mock private UpdateBranchUseCase updateBranchUseCase;
  @Mock private DeleteBranchUseCase deleteBranchUseCase;

  @InjectMocks private RbacServiceImpl rbacService;

  private UUID roleId;
  private UUID permissionId;
  private UUID userId;
  private UUID branchId;

  @BeforeEach
  void setUp() {
    roleId = UUID.randomUUID();
    permissionId = UUID.randomUUID();
    userId = UUID.randomUUID();
    branchId = UUID.randomUUID();
  }

  @Test
  @DisplayName("GetRoles should delegate to GetRolesUseCase")
  void getRoles_ShouldDelegateToUseCase() {
    // Arrange
    List<RoleResponse> expectedRoles =
        List.of(
            RoleResponse.builder().id(roleId).name(RoleName.ROLE_ADMIN).build(),
            RoleResponse.builder().id(UUID.randomUUID()).name(RoleName.ROLE_MARKETING).build());

    when(getRolesUseCase.execute()).thenReturn(expectedRoles);

    // Act
    List<RoleResponse> result = rbacService.getRoles();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(getRolesUseCase).execute();
  }

  @Test
  @DisplayName("CreateRole should delegate to CreateRoleUseCase")
  void createRole_ShouldDelegateToUseCase() {
    // Arrange
    CreateRoleRequest request =
        CreateRoleRequest.builder().name(RoleName.ROLE_ADMIN).description("Test Role").build();

    RoleResponse expectedResponse =
        RoleResponse.builder()
            .id(roleId)
            .name(RoleName.ROLE_ADMIN)
            .description("Test Role")
            .build();

    when(createRoleUseCase.execute(any(CreateRoleRequest.class))).thenReturn(expectedResponse);

    // Act
    RoleResponse result = rbacService.createRole(request);

    // Assert
    assertNotNull(result);
    assertEquals(RoleName.ROLE_ADMIN, result.getName());
    verify(createRoleUseCase).execute(request);
  }

  @Test
  @DisplayName("UpdateRole should delegate to UpdateRoleUseCase")
  void updateRole_ShouldDelegateToUseCase() {
    // Arrange
    UpdateRoleRequest request =
        UpdateRoleRequest.builder()
            .name(RoleName.ROLE_SUPER_ADMIN)
            .description("Updated Role")
            .build();

    RoleResponse expectedResponse =
        RoleResponse.builder()
            .id(roleId)
            .name(RoleName.ROLE_SUPER_ADMIN)
            .description("Updated Role")
            .build();

    when(updateRoleUseCase.execute(any(UUID.class), any(UpdateRoleRequest.class)))
        .thenReturn(expectedResponse);

    // Act
    RoleResponse result = rbacService.updateRole(roleId, request);

    // Assert
    assertNotNull(result);
    assertEquals(RoleName.ROLE_SUPER_ADMIN, result.getName());
    verify(updateRoleUseCase).execute(roleId, request);
  }

  @Test
  @DisplayName("DeleteRole should delegate to DeleteRoleUseCase")
  void deleteRole_ShouldDelegateToUseCase() {
    // Arrange
    doNothing().when(deleteRoleUseCase).execute(roleId);

    // Act
    rbacService.deleteRole(roleId);

    // Assert
    verify(deleteRoleUseCase).execute(roleId);
  }

  @Test
  @DisplayName("GetPermissions should delegate to GetPermissionsUseCase")
  void getPermissions_ShouldDelegateToUseCase() {
    // Arrange
    List<PermissionResponse> expectedPermissions =
        List.of(
            PermissionResponse.builder().id(permissionId).name("READ").build(),
            PermissionResponse.builder().id(UUID.randomUUID()).name("WRITE").build());

    when(getPermissionsUseCase.execute()).thenReturn(expectedPermissions);

    // Act
    List<PermissionResponse> result = rbacService.getPermissions();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    verify(getPermissionsUseCase).execute();
  }

  @Test
  @DisplayName("GetRolePermissions should delegate to GetRolePermissionsUseCase")
  void getRolePermissions_ShouldDelegateToUseCase() {
    // Arrange
    List<PermissionResponse> expectedPermissions =
        List.of(PermissionResponse.builder().id(permissionId).name("READ").build());

    when(getRolePermissionsUseCase.execute(roleId)).thenReturn(expectedPermissions);

    // Act
    List<PermissionResponse> result = rbacService.getRolePermissions(roleId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(getRolePermissionsUseCase).execute(roleId);
  }

  @Test
  @DisplayName("AssignPermissionsToRole should delegate to AssignPermissionsToRoleUseCase")
  void assignPermissionsToRole_ShouldDelegateToUseCase() {
    // Arrange
    AssignPermissionsRequest request =
        AssignPermissionsRequest.builder().permissionIds(List.of(permissionId)).build();

    doNothing().when(assignPermissionsToRoleUseCase).execute(any(UUID.class), any());

    // Act
    rbacService.assignPermissionsToRole(roleId, request);

    // Assert
    verify(assignPermissionsToRoleUseCase).execute(roleId, request);
  }

  @Test
  @DisplayName("RemovePermissionFromRole should delegate to RemovePermissionFromRoleUseCase")
  void removePermissionFromRole_ShouldDelegateToUseCase() {
    // Arrange
    doNothing().when(removePermissionFromRoleUseCase).execute(roleId, permissionId);

    // Act
    rbacService.removePermissionFromRole(roleId, permissionId);

    // Assert
    verify(removePermissionFromRoleUseCase).execute(roleId, permissionId);
  }

  @Test
  @DisplayName("GetUserRoles should delegate to GetUserRolesUseCase")
  void getUserRoles_ShouldDelegateToUseCase() {
    // Arrange
    List<RoleResponse> expectedRoles =
        List.of(RoleResponse.builder().id(roleId).name(RoleName.ROLE_MARKETING).build());

    when(getUserRolesUseCase.execute(userId)).thenReturn(expectedRoles);

    // Act
    List<RoleResponse> result = rbacService.getUserRoles(userId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(getUserRolesUseCase).execute(userId);
  }

  @Test
  @DisplayName("AssignRolesToUser should delegate to AssignRolesToUserUseCase")
  void assignRolesToUser_ShouldDelegateToUseCase() {
    // Arrange
    AssignRolesRequest request = AssignRolesRequest.builder().roleIds(List.of(roleId)).build();

    doNothing().when(assignRolesToUserUseCase).execute(any(UUID.class), any());

    // Act
    rbacService.assignRolesToUser(userId, request);

    // Assert
    verify(assignRolesToUserUseCase).execute(userId, request);
  }

  @Test
  @DisplayName("RemoveRoleFromUser should delegate to RemoveRoleFromUserUseCase")
  void removeRoleFromUser_ShouldDelegateToUseCase() {
    // Arrange
    doNothing().when(removeRoleFromUserUseCase).execute(userId, roleId);

    // Act
    rbacService.removeRoleFromUser(userId, roleId);

    // Assert
    verify(removeRoleFromUserUseCase).execute(userId, roleId);
  }

  @Test
  @DisplayName("GetBranches should delegate to GetBranchesUseCase")
  void getBranches_ShouldDelegateToUseCase() {
    // Arrange
    List<BranchResponse> expectedBranches =
        List.of(BranchResponse.builder().id(branchId).name("Main Branch").city("Jakarta").build());

    when(getBranchesUseCase.execute()).thenReturn(expectedBranches);

    // Act
    List<BranchResponse> result = rbacService.getBranches();

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    verify(getBranchesUseCase).execute();
  }

  @Test
  @DisplayName("CreateBranch should delegate to CreateBranchUseCase")
  void createBranch_ShouldDelegateToUseCase() {
    // Arrange
    CreateBranchRequest request =
        CreateBranchRequest.builder()
            .name("New Branch")
            .address("123 Street")
            .city("Jakarta")
            .build();

    BranchResponse expectedResponse =
        BranchResponse.builder()
            .id(branchId)
            .name("New Branch")
            .address("123 Street")
            .city("Jakarta")
            .build();

    when(createBranchUseCase.execute(any(CreateBranchRequest.class))).thenReturn(expectedResponse);

    // Act
    BranchResponse result = rbacService.createBranch(request);

    // Assert
    assertNotNull(result);
    assertEquals("New Branch", result.getName());
    verify(createBranchUseCase).execute(request);
  }

  @Test
  @DisplayName("UpdateBranch should delegate to UpdateBranchUseCase")
  void updateBranch_ShouldDelegateToUseCase() {
    // Arrange
    CreateBranchRequest request =
        CreateBranchRequest.builder()
            .name("Updated Branch")
            .address("456 Street")
            .city("Surabaya")
            .build();

    BranchResponse expectedResponse =
        BranchResponse.builder()
            .id(branchId)
            .name("Updated Branch")
            .address("456 Street")
            .city("Surabaya")
            .build();

    when(updateBranchUseCase.execute(any(UUID.class), any(CreateBranchRequest.class)))
        .thenReturn(expectedResponse);

    // Act
    BranchResponse result = rbacService.updateBranch(branchId, request);

    // Assert
    assertNotNull(result);
    assertEquals("Updated Branch", result.getName());
    verify(updateBranchUseCase).execute(branchId, request);
  }

  @Test
  @DisplayName("DeleteBranch should delegate to DeleteBranchUseCase")
  void deleteBranch_ShouldDelegateToUseCase() {
    // Arrange
    doNothing().when(deleteBranchUseCase).execute(branchId);

    // Act
    rbacService.deleteBranch(branchId);

    // Assert
    verify(deleteBranchUseCase).execute(branchId);
  }
}
