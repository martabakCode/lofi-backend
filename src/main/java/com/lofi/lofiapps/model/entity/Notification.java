package com.lofi.lofiapps.model.entity;

import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseDomainEntity {
  private UUID userId;
  private String title;
  private String message;
  private String type; // e.g., LOAN_STATUS, SECURITY, etc.
  @Builder.Default private Boolean isRead = false;
  private String link; // Optional link to a page
}
