package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.enums.RoleName;
import com.lofi.lofiapps.service.RbacService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RbacControllerTest {

  private MockMvc mockMvc;

  @Mock private RbacService rbacService;

  @InjectMocks private RbacController rbacController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(rbacController).build();
    objectMapper = new ObjectMapper();
  }

  // --- Role Management Tests ---

  @Test
  @DisplayName("Get roles should return list of roles")
  void getRoles_ShouldReturnListOfRoles() throws Exception {
    when(rbacService.getRoles()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/rbac/roles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).getRoles();
  }

  @Test
  @DisplayName("Create role should return created role")
  void createRole_ShouldReturnCreatedRole() throws Exception {
    CreateRoleRequest request = new CreateRoleRequest();
    request.setName(RoleName.ROLE_CUSTOMER);

    RoleResponse response = RoleResponse.builder().name(RoleName.ROLE_CUSTOMER).build();

    when(rbacService.createRole(any(CreateRoleRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/rbac/roles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).createRole(any(CreateRoleRequest.class));
  }

  @Test
  @DisplayName("Update role should return updated role")
  void updateRole_ShouldReturnUpdatedRole() throws Exception {
    UUID roleId = UUID.randomUUID();
    UpdateRoleRequest request = new UpdateRoleRequest();
    request.setDescription("Updated Description");

    RoleResponse response = RoleResponse.builder().name(RoleName.ROLE_ADMIN).build();

    when(rbacService.updateRole(eq(roleId), any(UpdateRoleRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            put("/rbac/roles/{roleId}", roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).updateRole(eq(roleId), any(UpdateRoleRequest.class));
  }

  @Test
  @DisplayName("Delete role should return success")
  void deleteRole_ShouldReturnSuccess() throws Exception {
    UUID roleId = UUID.randomUUID();
    doNothing().when(rbacService).deleteRole(roleId);

    mockMvc
        .perform(delete("/rbac/roles/{roleId}", roleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).deleteRole(roleId);
  }

  // --- Permission Management Tests ---

  @Test
  @DisplayName("Get permissions should return list of permissions")
  void getPermissions_ShouldReturnListOfPermissions() throws Exception {
    when(rbacService.getPermissions()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/rbac/permissions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).getPermissions();
  }

  // --- Role-Permission Mapping Tests ---

  @Test
  @DisplayName("Get role permissions should return list of permissions")
  void getRolePermissions_ShouldReturnListOfPermissions() throws Exception {
    UUID roleId = UUID.randomUUID();
    when(rbacService.getRolePermissions(roleId)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/rbac/roles/{roleId}/permissions", roleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).getRolePermissions(roleId);
  }

  @Test
  @DisplayName("Assign permissions should return success")
  void assignPermissions_ShouldReturnSuccess() throws Exception {
    UUID roleId = UUID.randomUUID();
    AssignPermissionsRequest request = new AssignPermissionsRequest();
    request.setPermissionIds(List.of(UUID.randomUUID()));

    doNothing()
        .when(rbacService)
        .assignPermissionsToRole(eq(roleId), any(AssignPermissionsRequest.class));

    mockMvc
        .perform(
            post("/rbac/roles/{roleId}/permissions", roleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1))
        .assignPermissionsToRole(eq(roleId), any(AssignPermissionsRequest.class));
  }

  @Test
  @DisplayName("Remove permission should return success")
  void removePermission_ShouldReturnSuccess() throws Exception {
    UUID roleId = UUID.randomUUID();
    UUID permissionId = UUID.randomUUID();

    doNothing().when(rbacService).removePermissionFromRole(roleId, permissionId);

    mockMvc
        .perform(delete("/rbac/roles/{roleId}/permissions/{permissionId}", roleId, permissionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).removePermissionFromRole(roleId, permissionId);
  }

  // --- User-Role Mapping Tests ---

  @Test
  @DisplayName("Get user roles should return list of roles")
  void getUserRoles_ShouldReturnListOfRoles() throws Exception {
    UUID userId = UUID.randomUUID();
    when(rbacService.getUserRoles(userId)).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/rbac/users/{userId}/roles", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).getUserRoles(userId);
  }

  @Test
  @DisplayName("Assign roles should return success")
  void assignRoles_ShouldReturnSuccess() throws Exception {
    UUID userId = UUID.randomUUID();
    AssignRolesRequest request = new AssignRolesRequest();
    request.setRoleIds(List.of(UUID.randomUUID()));

    doNothing().when(rbacService).assignRolesToUser(eq(userId), any(AssignRolesRequest.class));

    mockMvc
        .perform(
            post("/rbac/users/{userId}/roles", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).assignRolesToUser(eq(userId), any(AssignRolesRequest.class));
  }

  @Test
  @DisplayName("Remove role from user should return success")
  void removeRole_ShouldReturnSuccess() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();

    doNothing().when(rbacService).removeRoleFromUser(userId, roleId);

    mockMvc
        .perform(delete("/rbac/users/{userId}/roles/{roleId}", userId, roleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).removeRoleFromUser(userId, roleId);
  }

  // --- Branch Management Tests ---

  @Test
  @DisplayName("Get branches should return list of branches")
  void getBranches_ShouldReturnListOfBranches() throws Exception {
    when(rbacService.getBranches()).thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/rbac/branches"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).getBranches();
  }

  @Test
  @DisplayName("Create branch should return created branch")
  void createBranch_ShouldReturnCreatedBranch() throws Exception {
    CreateBranchRequest request = new CreateBranchRequest();
    request.setName("Branch 1");
    // Add other required fields
    request.setAddress("Addr");
    request.setCity("City");
    request.setState("State");
    request.setZipCode("12345");
    request.setPhone("08123");

    BranchResponse response = new BranchResponse();
    response.setName("Branch 1");

    when(rbacService.createBranch(any(CreateBranchRequest.class))).thenReturn(response);

    mockMvc
        .perform(
            post("/rbac/branches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).createBranch(any(CreateBranchRequest.class));
  }

  @Test
  @DisplayName("Update branch should return updated branch")
  void updateBranch_ShouldReturnUpdatedBranch() throws Exception {
    UUID branchId = UUID.randomUUID();
    CreateBranchRequest request = new CreateBranchRequest();
    request.setName("Branch Updated");
    request.setAddress("Addr");
    request.setCity("City");
    request.setState("State");
    request.setZipCode("12345");
    request.setPhone("08123");

    BranchResponse response = new BranchResponse();
    response.setName("Branch Updated");

    when(rbacService.updateBranch(eq(branchId), any(CreateBranchRequest.class)))
        .thenReturn(response);

    mockMvc
        .perform(
            put("/rbac/branches/{branchId}", branchId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).updateBranch(eq(branchId), any(CreateBranchRequest.class));
  }

  @Test
  @DisplayName("Delete branch should return success")
  void deleteBranch_ShouldReturnSuccess() throws Exception {
    UUID branchId = UUID.randomUUID();
    doNothing().when(rbacService).deleteBranch(branchId);

    mockMvc
        .perform(delete("/rbac/branches/{branchId}", branchId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(rbacService, times(1)).deleteBranch(branchId);
  }
}
