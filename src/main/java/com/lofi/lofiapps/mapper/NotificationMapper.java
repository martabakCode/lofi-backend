package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaNotification;
import com.lofi.lofiapps.model.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

  public Notification toDomainEntity(JpaNotification entity) {
    if (entity == null) return null;
    return Notification.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .userId(entity.getUserId())
        .title(entity.getTitle())
        .message(entity.getMessage())
        .type(entity.getType())
        .isRead(entity.getIsRead())
        .link(entity.getLink())
        .build();
  }

  public JpaNotification toJpaEntity(Notification domain) {
    if (domain == null) return null;
    return JpaNotification.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .userId(domain.getUserId())
        .title(domain.getTitle())
        .message(domain.getMessage())
        .type(domain.getType())
        .isRead(domain.getIsRead())
        .link(domain.getLink())
        .build();
  }
}
