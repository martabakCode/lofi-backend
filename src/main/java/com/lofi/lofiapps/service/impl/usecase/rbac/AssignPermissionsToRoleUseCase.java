package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.request.AssignPermissionsRequest;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.PermissionRepository;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssignPermissionsToRoleUseCase {
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;

  @Transactional
  public void execute(UUID roleId, AssignPermissionsRequest request) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());

    if (permissions.size() != request.getPermissionIds().size()) {
      throw new ResourceNotFoundException(
          "Permission", "ids", request.getPermissionIds().toString());
    }

    if (role.getPermissions() == null) {
      role.setPermissions(new HashSet<>());
    }

    role.getPermissions().addAll(permissions);
    roleRepository.save(role);
  }
}
