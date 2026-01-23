package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.dto.response.PermissionResponse;
import com.lofi.lofiapps.entity.Permission;
import com.lofi.lofiapps.repository.PermissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPermissionsUseCase {
  private final PermissionRepository permissionRepository;

  public List<PermissionResponse> execute() {
    return permissionRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private PermissionResponse mapToResponse(Permission permission) {
    return PermissionResponse.builder()
        .id(permission.getId())
        .name(permission.getName())
        .description(permission.getDescription())
        .build();
  }
}
