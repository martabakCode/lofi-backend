package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.LoanStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
  private String accessToken;
  private String refreshToken;
  private long expiresIn;
  private String tokenType;
  private Boolean pinSet;
  private Boolean profileCompleted;

  private Boolean hasSubmittedLoan;
  private LoanStatus activeLoanStatus;
  private BigDecimal activeLoanAmount;
  private BigDecimal availableProductLimit;
}
