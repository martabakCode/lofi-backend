package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "approval_history")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JpaApprovalHistory extends JpaBaseEntity {

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
