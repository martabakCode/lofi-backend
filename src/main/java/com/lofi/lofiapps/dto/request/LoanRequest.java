package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class LoanRequest {
  @NotNull(message = "Product ID is required")
  private UUID productId;

  @NotNull(message = "Loan Amount is required")
  @Positive
  private BigDecimal loanAmount;

  @NotNull(message = "Tenor is required")
  @Positive
  private Integer tenor;
}
