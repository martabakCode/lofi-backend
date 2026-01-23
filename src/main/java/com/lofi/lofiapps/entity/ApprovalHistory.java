package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE approval_history SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ApprovalHistory extends BaseEntity {

  @NotNull
  @Column(nullable = false)
  private UUID loanId;

  @Enumerated(EnumType.STRING)
  private LoanStatus fromStatus;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoanStatus toStatus;

  @NotNull
  @Column(nullable = false)
  private String actionBy;

  private String notes;
}
