package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import java.util.List;
import java.util.UUID;

public interface RbacService {
  // Roles
  List<RoleResponse> getRoles();

  RoleResponse createRole(CreateRoleRequest request);

  RoleResponse updateRole(UUID roleId, UpdateRoleRequest request);

  void deleteRole(UUID roleId);

  // Permissions
  List<PermissionResponse> getPermissions();

  List<PermissionResponse> getRolePermissions(UUID roleId);

  void assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request);

  void removePermissionFromRole(UUID roleId, UUID permissionId);

  // User Roles
  List<RoleResponse> getUserRoles(UUID userId);

  void assignRolesToUser(UUID userId, AssignRolesRequest request);

  void removeRoleFromUser(UUID userId, UUID roleId);

  // Branches
  List<BranchResponse> getBranches();

  BranchResponse createBranch(CreateBranchRequest request);

  BranchResponse updateBranch(UUID branchId, CreateBranchRequest request);

  void deleteBranch(UUID branchId);
}
