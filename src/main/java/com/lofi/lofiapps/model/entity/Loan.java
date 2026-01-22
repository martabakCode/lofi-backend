package com.lofi.lofiapps.model.entity;

import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Loan extends BaseDomainEntity {
  private User customer;
  private Branch branch;
  private Product product;
  private BigDecimal loanAmount;
  private Integer tenor;
  private LoanStatus loanStatus;
  private ApprovalStage currentStage;
  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private LocalDateTime disbursedAt;
  private String disbursementReference;
  private LocalDateTime lastStatusChangedAt;
}
