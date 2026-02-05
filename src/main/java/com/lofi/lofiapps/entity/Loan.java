package com.lofi.lofiapps.entity;

import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
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
public class Loan extends BaseEntity {

  @NotNull(message = "Customer is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private User customer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "branch_id")
  private Branch branch;

  @NotNull(message = "Product is required")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

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

  @Column(precision = 11, scale = 8)
  private BigDecimal longitude;

  @Column(precision = 11, scale = 8)
  private BigDecimal latitude;

  // Income and NPWP
  private BigDecimal declaredIncome;
  private String npwpNumber;

  // Employment/Business Details
  @Enumerated(EnumType.STRING)
  private com.lofi.lofiapps.enums.JobType jobType;

  private String companyName;
  private String jobPosition;
  private Integer workDurationMonths;
  private String workAddress;
  private String officePhoneNumber;
  private BigDecimal additionalIncome;

  // Emergency Contact
  private String emergencyContactName;
  private String emergencyContactRelation;
  private String emergencyContactPhone;
  private String emergencyContactAddress;

  // Down Payment
  private BigDecimal downPayment;

  // Loan Purpose (Alasan Meminjam)
  @Column(columnDefinition = "TEXT")
  private String purpose;

  // Bank Account Information for Disbursement
  private String bankName;
  private String bankBranch;
  private String accountNumber;
  private String accountHolderName;

  // Snapshot of product rates at loan creation (to prevent changes when product
  // is edited)
  @Column(precision = 5, scale = 2)
  private BigDecimal interestRate;

  private BigDecimal adminFee;

  @Column(columnDefinition = "BIT DEFAULT 0")
  private Boolean pinValidated;
}
