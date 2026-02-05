package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {
  @NotNull(message = "Loan Amount is required")
  @Positive
  private BigDecimal loanAmount;

  @NotNull(message = "Tenor is required")
  @Positive
  private Integer tenor;

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

  /**
   * Optional PIN for validation before loan submission. If provided, PIN will be validated. If not
   * provided, loan will proceed without PIN validation.
   */
  @jakarta.validation.constraints.Pattern(regexp = "^\\d{4,6}$", message = "PIN must be 4-6 digits")
  private String pin;
}
