package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/rbac")
@RequiredArgsConstructor
@Tag(name = "RBAC", description = "Role-Based Access Control")
public class RbacController {

  private final RbacService rbacService;

  // --- Role Management ---

  @GetMapping("/roles")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get all roles")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getRoles() {
    return ResponseEntity.ok(ApiResponse.success(rbacService.getRoles(), "Roles retrieved"));
  }

  @PostMapping("/roles")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new role")
  public ResponseEntity<ApiResponse<RoleResponse>> createRole(
      @Valid @RequestBody CreateRoleRequest request) {
    return ResponseEntity.ok(ApiResponse.success(rbacService.createRole(request), "Role created"));
  }

  @PutMapping("/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update a role")
  public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
      @PathVariable UUID roleId, @Valid @RequestBody UpdateRoleRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.updateRole(roleId, request), "Role updated"));
  }

  @DeleteMapping("/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Delete a role")
  public ResponseEntity<ApiResponse<Object>> deleteRole(@PathVariable UUID roleId) {
    rbacService.deleteRole(roleId);
    return ResponseEntity.ok(ApiResponse.success(null, "Role deleted"));
  }

  // --- Permission Management ---

  @GetMapping("/permissions")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get all permissions")
  public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.getPermissions(), "Permissions retrieved"));
  }

  // --- Role-Permission Mapping ---

  @GetMapping("/roles/{roleId}/permissions")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get role permissions")
  public ResponseEntity<ApiResponse<List<PermissionResponse>>> getRolePermissions(
      @PathVariable UUID roleId) {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.getRolePermissions(roleId), "Role permissions retrieved"));
  }

  @PostMapping("/roles/{roleId}/permissions")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Assign permissions to a role")
  public ResponseEntity<ApiResponse<Object>> assignPermissions(
      @PathVariable UUID roleId, @Valid @RequestBody AssignPermissionsRequest request) {
    rbacService.assignPermissionsToRole(roleId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Permissions assigned to role"));
  }

  @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Remove a permission from a role")
  public ResponseEntity<ApiResponse<Object>> removePermission(
      @PathVariable UUID roleId, @PathVariable UUID permissionId) {
    rbacService.removePermissionFromRole(roleId, permissionId);
    return ResponseEntity.ok(ApiResponse.success(null, "Permission removed from role"));
  }

  // --- User-Role Mapping ---

  @GetMapping("/users/{userId}/roles")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get user roles")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(@PathVariable UUID userId) {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.getUserRoles(userId), "User roles retrieved"));
  }

  @PostMapping("/users/{userId}/roles")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Assign roles to a user")
  public ResponseEntity<ApiResponse<Object>> assignRoles(
      @PathVariable UUID userId, @Valid @RequestBody AssignRolesRequest request) {
    rbacService.assignRolesToUser(userId, request);
    return ResponseEntity.ok(ApiResponse.success(null, "Roles assigned to user"));
  }

  @DeleteMapping("/users/{userId}/roles/{roleId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Remove a role from a user")
  public ResponseEntity<ApiResponse<Object>> removeRole(
      @PathVariable UUID userId, @PathVariable UUID roleId) {
    rbacService.removeRoleFromUser(userId, roleId);
    return ResponseEntity.ok(ApiResponse.success(null, "Role removed from user"));
  }

  // --- Branch Management ---

  @GetMapping("/branches")
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get all branches")
  public ResponseEntity<ApiResponse<List<BranchResponse>>> getBranches() {
    return ResponseEntity.ok(ApiResponse.success(rbacService.getBranches(), "Branches retrieved"));
  }

  @PostMapping("/branches")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Create a new branch")
  public ResponseEntity<ApiResponse<BranchResponse>> createBranch(
      @Valid @RequestBody CreateBranchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.createBranch(request), "Branch created"));
  }

  @PutMapping("/branches/{branchId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Update a branch")
  public ResponseEntity<ApiResponse<BranchResponse>> updateBranch(
      @PathVariable UUID branchId, @Valid @RequestBody CreateBranchRequest request) {
    return ResponseEntity.ok(
        ApiResponse.success(rbacService.updateBranch(branchId, request), "Branch updated"));
  }

  @DeleteMapping("/branches/{branchId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @Operation(summary = "Delete a branch")
  public ResponseEntity<ApiResponse<Object>> deleteBranch(@PathVariable UUID branchId) {
    rbacService.deleteBranch(branchId);
    return ResponseEntity.ok(ApiResponse.success(null, "Branch deleted"));
  }
}
