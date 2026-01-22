package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.model.dto.request.*;
import com.lofi.lofiapps.model.dto.response.*;
import com.lofi.lofiapps.service.impl.rbac.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
public class RbacController {

  private final GetRolesUseCase getRolesUseCase;
  private final CreateRoleUseCase createRoleUseCase;
  private final UpdateRoleUseCase updateRoleUseCase;
  private final DeleteRoleUseCase deleteRoleUseCase;
  private final GetPermissionsUseCase getPermissionsUseCase;
  private final GetRolePermissionsUseCase getRolePermissionsUseCase;
  private final AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;
  private final RemovePermissionFromRoleUseCase removePermissionFromRoleUseCase;
  private final GetUserRolesUseCase getUserRolesUseCase;
  private final AssignRolesToUserUseCase assignRolesToUserUseCase;
  private final RemoveRoleFromUserUseCase removeRoleFromUserUseCase;
  private final GetBranchesUseCase getBranchesUseCase;
  private final CreateBranchUseCase createBranchUseCase;
  private final UpdateBranchUseCase updateBranchUseCase;
  private final DeleteBranchUseCase deleteBranchUseCase;

  // --- Role Management ---

  @GetMapping("/roles")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
    return ResponseEntity.ok(ApiResponse.success(getRolesUseCase.execute(), "Roles retrieved"));
  }

  @PostMapping("/roles")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<RoleResponse>> createRole(
      @Valid @RequestBody CreateRoleRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(createRoleUseCase.execute(request), "Role created"));
  }

  @PutMapping("/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
      @PathVariable UUID roleId, @Valid @RequestBody UpdateRoleRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(updateRoleUseCase.execute(roleId, request), "Role updated"));
  }

  @DeleteMapping("/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> deleteRole(@PathVariable UUID roleId) {
    deleteRoleUseCase.execute(roleId);
    return ResponseEntity.ok(ApiResponse.success(null, "Role deleted"));
  }

  // --- Permission Management ---

  @GetMapping("/permissions")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
    return ResponseEntity.ok(
        ApiResponse.success(getPermissionsUseCase.execute(), "Permissions retrieved"));
  }

  // --- Role-Permission Mapping ---

  @GetMapping("/roles/{roleId}/permissions")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
      @PathVariable UUID roleId) {
    return ResponseEntity.ok(
        ApiResponse.success(
            getRolePermissionsUseCase.execute(roleId), "Role permissions retrieved"));
  }

  @PostMapping("/roles/{roleId}/permissions")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> assignPermissions(
      @PathVariable UUID roleId, @Valid @RequestBody AssignPermissionsRequest request) {
    assignPermissionsToRoleUseCase.execute(roleId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Permissions assigned to role"));
  }

  @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> removePermission(
      @PathVariable UUID roleId, @PathVariable UUID permissionId) {
    removePermissionFromRoleUseCase.execute(roleId, permissionId);
    return ResponseEntity.ok(ApiResponse.success(null, "Permission removed from role"));
  }

  // --- User-Role Mapping ---

  @GetMapping("/users/{userId}/roles")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(@PathVariable UUID userId) {
    return ResponseEntity.ok(
        ApiResponse.success(getUserRolesUseCase.execute(userId), "User roles retrieved"));
  }

  @PostMapping("/users/{userId}/roles")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> assignRoles(
      @PathVariable UUID userId, @Valid @RequestBody AssignRolesRequest request) {
    assignRolesToUserUseCase.execute(userId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Roles assigned to user"));
  }

  @DeleteMapping("/users/{userId}/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> removeRole(
      @PathVariable UUID userId, @PathVariable UUID roleId) {
    removeRoleFromUserUseCase.execute(userId, roleId);
    return ResponseEntity.ok(ApiResponse.success(null, "Role removed from user"));
  }

  // --- Branch Management ---

  @GetMapping("/branches")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches() {
    return ResponseEntity.ok(
        ApiResponse.success(getBranchesUseCase.execute(), "Branches retrieved"));
  }

  @PostMapping("/branches")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
      @Valid @RequestBody CreateBranchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(createBranchUseCase.execute(request), "Branch created"));
  }

  @PutMapping("/branches/{branchId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
      @PathVariable UUID branchId, @Valid @RequestBody CreateBranchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(updateBranchUseCase.execute(branchId, request), "Branch updated"));
  }

  @DeleteMapping("/branches/{branchId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<ApiResponse<Object>> deleteBranch(@PathVariable UUID branchId) {
    deleteBranchUseCase.execute(branchId);
    return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted"));
  }
}
