package com.lofi.lofiapps.service.impl.usecase.rbac;

import com.lofi.lofiapps.dto.response.PermissionResponse;
import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Role;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.RoleRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetRoleByIdUseCase {
  private final RoleRepository roleRepository;

  @Transactional(readOnly = true)
  public RoleResponse execute(UUID roleId) {
    Role role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    List<PermissionResponse> permissionResponses =
        role.getPermissions() != null
            ? role.getPermissions().stream()
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
        .id(role.getId())
        .name(role.getName())
        .description(role.getDescription())
        .permissions(permissionResponses)
        .build();
  }
}
