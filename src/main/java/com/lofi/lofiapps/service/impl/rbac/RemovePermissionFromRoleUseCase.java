package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.entity.JpaPermission;
import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.repository.JpaPermissionRepository;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RemovePermissionFromRoleUseCase {
  private final JpaRoleRepository roleRepository;
  private final JpaPermissionRepository permissionRepository;

  @Transactional
  public void execute(UUID roleId, UUID permissionId) {
    JpaRole role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    JpaPermission permission =
        permissionRepository
            .findById(permissionId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Permission", "id", permissionId.toString()));

    if (role.getPermissions() != null) {
      role.getPermissions().remove(permission);
      roleRepository.save(role);
    }
  }
}
