package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateProductRequest {
  @NotBlank(message = "Product Code is required")
  private String productCode;

  @NotBlank(message = "Product Name is required")
  private String productName;

  private String description;

  @NotNull(message = "Interest Rate is required")
  @Positive
  private BigDecimal interestRate;

  @NotNull(message = "Admin Fee is required")
  @Positive
  private BigDecimal adminFee;

  @NotNull(message = "Min Tenor is required")
  @Positive
  private Integer minTenor;

  @NotNull(message = "Max Tenor is required")
  @Positive
  private Integer maxTenor;

  @NotNull(message = "Min Loan Amount is required")
  @Positive
  private BigDecimal minLoanAmount;

  @NotNull(message = "Max Loan Amount is required")
  @Positive
  private BigDecimal maxLoanAmount;
}
