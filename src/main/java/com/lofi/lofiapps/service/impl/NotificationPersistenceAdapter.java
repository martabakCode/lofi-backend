package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.mapper.NotificationMapper;
import com.lofi.lofiapps.model.entity.JpaNotification;
import com.lofi.lofiapps.model.entity.Notification;
import com.lofi.lofiapps.repository.JpaNotificationRepository;
import com.lofi.lofiapps.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements NotificationRepository {

  private final JpaNotificationRepository repository;
  private final NotificationMapper mapper;

  @Override
  public Notification save(Notification notification) {
    JpaNotification jpaEntity = mapper.toJpaEntity(notification);
    JpaNotification savedEntity = repository.save(jpaEntity);
    return mapper.toDomainEntity(savedEntity);
  }

  @Override
  public List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(mapper::toDomainEntity)
        .collect(Collectors.toList());
  }

  @Override
  public long countUnreadByUserId(UUID userId) {
    return repository.countByUserIdAndIsReadFalse(userId);
  }

  @Override
  @Transactional
  public void markAsRead(UUID notificationId) {
    repository
        .findById(notificationId)
        .ifPresent(
            n -> {
              n.setIsRead(true);
              repository.save(n);
            });
  }

  @Override
  @Transactional
  public void markAllAsRead(UUID userId) {
    repository.markAllAsReadByUserId(userId);
  }
}
