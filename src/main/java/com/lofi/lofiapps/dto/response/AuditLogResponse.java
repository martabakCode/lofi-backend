package com.lofi.lofiapps.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditLogResponse {
  private UUID id;
  private UUID userId;
  private String action;
  private String resourceType;
  private String resourceId;
  private String details;
  private LocalDateTime createdAt;
}
