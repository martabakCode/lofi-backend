package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
  List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);

  long countByUserIdAndIsReadFalse(UUID userId);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
  void markAllAsReadByUserId(UUID userId);
}
