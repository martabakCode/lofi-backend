package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.dto.response.RiskItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Entity to store risk check results for loans. Provides audit trail for compliance and risk
 * management.
 */
@Entity
@Table(name = "risk_checks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE risk_checks SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class RiskCheck extends BaseEntity {

  @NotNull(message = "Loan ID is required")
  @Column(name = "loan_id", nullable = false)
  private UUID loanId;

  @NotNull(message = "Check type is required")
  @Column(name = "check_type", nullable = false, length = 50)
  private String checkType;

  @Column(name = "check_name", length = 100)
  private String checkName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @NotNull(message = "Status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RiskItem.RiskStatus status;

  @Column(name = "comments", columnDefinition = "TEXT")
  private String comments;

  @Column(name = "checked_at", nullable = false)
  private LocalDateTime checkedAt;

  @Column(name = "checked_by")
  private UUID checkedBy;

  @Column(name = "external_reference", length = 255)
  private String externalReference;

  @Column(name = "raw_response", columnDefinition = "TEXT")
  private String rawResponse;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  @Column(name = "resolved_by")
  private UUID resolvedBy;

  @Column(name = "resolution_comments", columnDefinition = "TEXT")
  private String resolutionComments;

  @Version private Long version;

  @PrePersist
  protected void onCreate() {
    if (checkedAt == null) {
      checkedAt = LocalDateTime.now();
    }
  }
}
