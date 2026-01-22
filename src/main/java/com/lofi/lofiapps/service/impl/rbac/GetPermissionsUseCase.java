package com.lofi.lofiapps.service.impl.rbac;

import com.lofi.lofiapps.model.dto.response.PermissionResponse;
import com.lofi.lofiapps.model.entity.JpaPermission;
import com.lofi.lofiapps.repository.JpaPermissionRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPermissionsUseCase {
  private final JpaPermissionRepository permissionRepository;

  public List<PermissionResponse> execute() {
    return permissionRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private PermissionResponse mapToResponse(JpaPermission permission) {
    return PermissionResponse.builder()
        .id(permission.getId())
        .name(permission.getName())
        .description(permission.getDescription())
        .build();
  }
}
