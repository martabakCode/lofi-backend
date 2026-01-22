package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaPermission;
import com.lofi.lofiapps.model.entity.Permission;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {
  public Permission toDomain(JpaPermission entity) {
    if (entity == null) return null;
    return Permission.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .name(entity.getName())
        .description(entity.getDescription())
        .build();
  }

  public JpaPermission toJpa(Permission domain) {
    if (domain == null) return null;
    return JpaPermission.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .name(domain.getName())
        .description(domain.getDescription())
        .build();
  }
}
