package com.lofi.lofiapps.service.impl.mapper;

import com.lofi.lofiapps.dto.response.PermissionResponse;
import com.lofi.lofiapps.entity.Permission;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

  public PermissionResponse toResponse(Permission permission) {
    if (permission == null) return null;
    return PermissionResponse.builder()
        .id(permission.getId())
        .name(permission.getName())
        .description(permission.getDescription())
        .build();
  }

  public List<PermissionResponse> toResponseList(Set<Permission> permissions) {
    if (permissions == null) return List.of();
    return permissions.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<PermissionResponse> toResponseList(List<Permission> permissions) {
    if (permissions == null) return List.of();
    return permissions.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
