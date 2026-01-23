package com.lofi.lofiapps.dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
  private UUID id;
  private String productCode;
  private String productName;
  private String description;
  private BigDecimal interestRate;
  private BigDecimal adminFee;
  private Integer minTenor;
  private Integer maxTenor;
  private BigDecimal minLoanAmount;
  private BigDecimal maxLoanAmount;
  private Boolean isActive;
}
