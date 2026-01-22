package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "loans")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE loans SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class JpaLoan extends JpaBaseEntity {

  @NotNull(message = "Customer is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private JpaUser customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  private JpaBranch branch;

  @NotNull(message = "Product is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private JpaProduct product;

  @NotNull(message = "Loan Amount is required")
  @Column(nullable = false)
  private BigDecimal loanAmount;

  @NotNull(message = "Tenor is required")
  @Column(nullable = false)
  private Integer tenor;

  @NotNull(message = "Loan Status is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LoanStatus loanStatus;

  @NotNull(message = "Current Stage is required")
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ApprovalStage currentStage;

  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private LocalDateTime disbursedAt;
  private String disbursementReference;
  private LocalDateTime lastStatusChangedAt;
}
