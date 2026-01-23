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

  private String resourceType;
  private String resourceId;

  @Column(columnDefinition = "TEXT")
  private String details;
}
