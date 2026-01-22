package com.lofi.lofiapps.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseDomainEntity {
  private UUID id;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private String createdBy;
  private String lastModifiedBy;
  private LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
