package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.*;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.service.RbacService;
import com.lofi.lofiapps.service.impl.usecase.rbac.*;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RbacServiceImpl implements RbacService {

  private final GetRolesUseCase getRolesUseCase;
  private final GetRoleByIdUseCase getRoleByIdUseCase;
  private final CreateRoleUseCase createRoleUseCase;
  private final UpdateRoleUseCase updateRoleUseCase;
  private final DeleteRoleUseCase deleteRoleUseCase;
  private final GetPermissionsUseCase getPermissionsUseCase;
  private final GetRolePermissionsUseCase getRolePermissionsUseCase;
  private final AssignPermissionsToRoleUseCase assignPermissionsToRoleUseCase;
  private final RemovePermissionFromRoleUseCase removePermissionFromRoleUseCase;
  private final GetRbacUsersUseCase getRbacUsersUseCase;
  private final GetUserRolesUseCase getUserRolesUseCase;
  private final AssignRolesToUserUseCase assignRolesToUserUseCase;
  private final RemoveRoleFromUserUseCase removeRoleFromUserUseCase;
  private final GetBranchesUseCase getBranchesUseCase;
  private final GetBranchByIdUseCase getBranchByIdUseCase;
  private final CreateBranchUseCase createBranchUseCase;
  private final UpdateBranchUseCase updateBranchUseCase;
  private final DeleteBranchUseCase deleteBranchUseCase;

  @Override
  @Transactional(readOnly = true)
  public List<RoleResponse> getRoles() {
    return getRolesUseCase.execute();
  }

  @Override
  @Transactional(readOnly = true)
  public RoleResponse getRoleById(UUID roleId) {
    return getRoleByIdUseCase.execute(roleId);
  }

  @Override
  @Transactional
  public RoleResponse createRole(CreateRoleRequest request) {
    return createRoleUseCase.execute(request);
  }

  @Override
  @Transactional
  public RoleResponse updateRole(UUID roleId, UpdateRoleRequest request) {
    return updateRoleUseCase.execute(roleId, request);
  }

  @Override
  @Transactional
  public void deleteRole(UUID roleId) {
    deleteRoleUseCase.execute(roleId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PermissionResponse> getPermissions() {
    return getPermissionsUseCase.execute();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PermissionResponse> getRolePermissions(UUID roleId) {
    return getRolePermissionsUseCase.execute(roleId);
  }

  @Override
  @Transactional
  public void assignPermissionsToRole(UUID roleId, AssignPermissionsRequest request) {
    assignPermissionsToRoleUseCase.execute(roleId, request);
  }

  @Override
  @Transactional
  public void removePermissionFromRole(UUID roleId, UUID permissionId) {
    removePermissionFromRoleUseCase.execute(roleId, permissionId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserSummaryResponse> getUsers() {
    return getRbacUsersUseCase.execute();
  }

  @Override
  @Transactional(readOnly = true)
  public List<RoleResponse> getUserRoles(UUID userId) {
    return getUserRolesUseCase.execute(userId);
  }

  @Override
  @Transactional
  public void assignRolesToUser(UUID userId, AssignRolesRequest request) {
    assignRolesToUserUseCase.execute(userId, request);
  }

  @Override
  @Transactional
  public void removeRoleFromUser(UUID userId, UUID roleId) {
    removeRoleFromUserUseCase.execute(userId, roleId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BranchResponse> getBranches() {
    return getBranchesUseCase.execute();
  }

  @Override
  @Transactional(readOnly = true)
  public BranchResponse getBranchById(UUID branchId) {
    return getBranchByIdUseCase.execute(branchId);
  }

  @Override
  @Transactional
  public BranchResponse createBranch(CreateBranchRequest request) {
    return createBranchUseCase.execute(request);
  }

  @Override
  @Transactional
  public BranchResponse updateBranch(UUID branchId, CreateBranchRequest request) {
    return updateBranchUseCase.execute(branchId, request);
  }

  @Override
  @Transactional
  public void deleteBranch(UUID branchId) {
    deleteBranchUseCase.execute(branchId);
  }
}
