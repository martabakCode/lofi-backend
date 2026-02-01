package com.lofi.lofiapps.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(updatable = false, nullable = false)
  private UUID id;

  @CreatedDate
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @LastModifiedBy private String lastModifiedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  public boolean isDeleted() {
    return deletedAt != null;
  }

  public void setDeleted(boolean deleted) {
    if (deleted) {
      if (this.deletedAt == null) {
        this.deletedAt = LocalDateTime.now();
      }
    } else {
      this.deletedAt = null;
    }
  }

  @PrePersist
  public void prePersist() {
    if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    if (this.createdBy == null) this.createdBy = "System";
    if (this.lastModifiedBy == null) this.lastModifiedBy = "System";
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
    if (this.lastModifiedBy == null) this.lastModifiedBy = "System";
  }
}
