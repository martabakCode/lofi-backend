package com.lofi.lofiapps.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {

  @Column(nullable = false)
  private UUID userId;

  @Column(nullable = false)
  private String action;

  private String entityType;
  private UUID entityId;

  // Alias fields for compatibility
  private String resourceType;
  private String resourceId;
  private String details;

  @Column(columnDefinition = "TEXT")
  private String oldValue;

  @Column(columnDefinition = "TEXT")
  private String newValue;

  private String ipAddress;
  private String userAgent;

  @Column(columnDefinition = "TEXT")
  private String description;
}
