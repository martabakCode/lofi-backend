package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaRole;
import com.lofi.lofiapps.model.entity.Role;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleMapper {
  private final PermissionMapper permissionMapper;

  public Role toDomain(JpaRole entity) {
    if (entity == null) return null;
    return Role.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .name(entity.getName())
        .permissions(
            entity.getPermissions() != null
                ? entity.getPermissions().stream()
                    .map(permissionMapper::toDomain)
                    .collect(Collectors.toSet())
                : null)
        .build();
  }

  public JpaRole toJpa(Role domain) {
    if (domain == null) return null;
    return JpaRole.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .name(domain.getName())
        .permissions(
            domain.getPermissions() != null
                ? domain.getPermissions().stream()
                    .map(permissionMapper::toJpa)
                    .collect(Collectors.toSet())
                : null)
        .build();
  }
}
