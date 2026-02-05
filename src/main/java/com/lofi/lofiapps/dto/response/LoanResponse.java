package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.ApprovalStage;
import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanResponse {
  private UUID id;
  private UUID customerId;
  private String customerName;
  private ProductResponse product;
  private BigDecimal loanAmount;
  private Integer tenor;
  private LoanStatus loanStatus;
  private ApprovalStage currentStage;
  private LocalDateTime submittedAt;
  private LocalDateTime approvedAt;
  private LocalDateTime rejectedAt;
  private LocalDateTime disbursedAt;
  private java.util.List<com.lofi.lofiapps.dto.response.DocumentResponse> documents;
  private String disbursementReference;
  private LoanAnalysisResponse aiAnalysis;
  private BigDecimal longitude;
  private BigDecimal latitude;

  // Income and NPWP
  private BigDecimal declaredIncome;
  private String npwpNumber;

  // Employment/Business Details
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

  // Loan Purpose
  private String purpose;

  // Bank Account Information for Disbursement
  private String bankName;
  private String bankBranch;
  private String accountNumber;
  private String accountHolderName;

  // Snapshot of product rates at loan creation
  private BigDecimal interestRate;
  private BigDecimal adminFee;

  /** Indicates whether PIN was validated during loan application. Null if PIN was not provided. */
  private Boolean pinValidated;
}
