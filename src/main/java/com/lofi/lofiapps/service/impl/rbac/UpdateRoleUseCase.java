package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.dto.request.UpdateRoleRequest;
import com.lofi.lofiapps.model.dto.response.PermissionResponse;
import com.lofi.lofiapps.model.dto.response.RoleResponse;
import com.lofi.lofiapps.model.entity.JpaPermission;
import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.repository.JpaPermissionRepository;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateRoleUseCase {
  private final JpaRoleRepository roleRepository;
  private final JpaPermissionRepository permissionRepository;

  @Transactional
  public RoleResponse execute(UUID roleId, UpdateRoleRequest request) {
    JpaRole role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    // Update description if provided
    if (request.getDescription() != null) {
      role.setDescription(request.getDescription());
    }

    // Update permissions if provided
    if (request.getPermissionIds() != null) {
      List<JpaPermission> permissions =
          permissionRepository.findAllById(request.getPermissionIds());
      role.setPermissions(new HashSet<>(permissions));
    }

    JpaRole saved = roleRepository.save(role);

    // Build permissions response
    List<PermissionResponse> permissionResponses =
        saved.getPermissions() != null
            ? saved.getPermissions().stream()
                .map(
                    p ->
                        PermissionResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .description(p.getDescription())
                            .build())
                .collect(Collectors.toList())
            : List.of();

    return RoleResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .description(saved.getDescription())
        .permissions(permissionResponses)
        .build();
  }
}
