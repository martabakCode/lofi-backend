package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableProductResponse {
  private UUID productId;
  private String productCode;
  private String productName;
  private BigDecimal productLimit; // Original maxLoanAmount from Product
  private BigDecimal approvedLoanAmount; // Sum of active loan amounts
  private BigDecimal availableAmount; // productLimit - approvedLoanAmount
  private Boolean hasSubmittedLoan;
  private LoanStatus lastLoanStatus;
  private LocalDateTime lastLoanSubmittedAt;
}
