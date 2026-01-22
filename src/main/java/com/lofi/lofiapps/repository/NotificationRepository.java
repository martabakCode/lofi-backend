package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository {
  Notification save(Notification notification);

  List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

  long countUnreadByUserId(UUID userId);

  void markAsRead(UUID notificationId);

  void markAllAsRead(UUID userId);
}
