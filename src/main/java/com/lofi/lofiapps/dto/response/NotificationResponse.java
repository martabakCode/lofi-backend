package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
  private UUID id;
  private UUID userId;
  private String title;
  private String body;
  private NotificationType type;
  private UUID referenceId;
  private Boolean isRead;
  private LocalDateTime createdAt;
  private String link;
}
