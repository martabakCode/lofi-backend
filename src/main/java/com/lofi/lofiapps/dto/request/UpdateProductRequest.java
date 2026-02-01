package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class UpdateProductRequest {
  @NotBlank(message = "Product name is required")
  private String productName;

  private String description;

  @NotNull(message = "Interest rate is required")
  @DecimalMin(value = "0.0", inclusive = false)
  private BigDecimal interestRate;

  @NotNull(message = "Minimum tenor is required")
  @Positive
  private Integer minTenor;

  @NotNull(message = "Maximum tenor is required")
  @Positive
  private Integer maxTenor;

  @NotNull(message = "Minimum loan amount is required")
  @Positive
  private BigDecimal minLoanAmount;

  @NotNull(message = "Maximum loan amount is required")
  @Positive
  private BigDecimal maxLoanAmount;

  @NotNull(message = "Admin fee is required")
  @Positive
  private BigDecimal adminFee;

  @NotNull(message = "Active status is required")
  private Boolean isActive;
}
