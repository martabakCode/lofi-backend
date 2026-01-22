package com.lofi.lofiapps.repository;

import com.lofi.lofiapps.model.entity.JpaNotification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaNotificationRepository extends JpaRepository<JpaNotification, UUID> {
  List<JpaNotification> findByUserIdOrderByCreatedAtDesc(UUID userId);

  long countByUserIdAndIsReadFalse(UUID userId);

  @Modifying
  @Query("UPDATE JpaNotification n SET n.isRead = true WHERE n.userId = :userId")
  void markAllAsReadByUserId(UUID userId);
}
