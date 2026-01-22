package com.lofi.lofiapps.model.entity;

import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseDomainEntity {
  private String productCode;
  private String productName;
  private String description;
  private BigDecimal interestRate;
  private BigDecimal adminFee;
  private Integer minTenor;
  private Integer maxTenor;
  private BigDecimal minLoanAmount;
  private BigDecimal maxLoanAmount;
  @Builder.Default private Boolean isActive = true;
}
