package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.dto.response.PermissionResponse;
import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.repository.JpaRoleRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetRolePermissionsUseCase {
  private final JpaRoleRepository roleRepository;

  public List<PermissionResponse> execute(UUID roleId) {
    JpaRole role =
        roleRepository
            .findById(roleId)
            .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId.toString()));

    return role.getPermissions().stream()
        .map(
            p ->
                PermissionResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .build())
        .collect(Collectors.toList());
  }
}
