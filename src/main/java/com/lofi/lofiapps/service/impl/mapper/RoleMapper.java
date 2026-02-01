package com.lofi.lofiapps.service.impl.mapper;

import com.lofi.lofiapps.dto.response.RoleResponse;
import com.lofi.lofiapps.entity.Role;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleMapper {

  private final PermissionMapper permissionMapper;

  public RoleResponse toResponse(Role role) {
    if (role == null) return null;
    return RoleResponse.builder()
        .id(role.getId())
        .name(role.getName())
        .description(role.getDescription())
        .permissions(permissionMapper.toResponseList(role.getPermissions()))
        .build();
  }

  public List<RoleResponse> toResponseList(Set<Role> roles) {
    if (roles == null) return List.of();
    return roles.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<RoleResponse> toResponseList(List<Role> roles) {
    if (roles == null) return List.of();
    return roles.stream().map(this::toResponse).collect(Collectors.toList());
  }
}
