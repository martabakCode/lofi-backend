package com.lofi.lofiapps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product extends BaseEntity {

  @NotBlank(message = "Product name is required")
  @Column(nullable = false)
  private String productName;

  @NotBlank(message = "Product code is required")
  @Column(nullable = false, unique = true)
  private String productCode;

  @Column(columnDefinition = "TEXT")
  private String description;

  @NotNull(message = "Interest rate is required")
  @DecimalMin(value = "0.0", inclusive = false)
  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal interestRate;

  @NotNull(message = "Minimum tenor is required")
  @Column(nullable = false)
  private Integer minTenor;

  @NotNull(message = "Maximum tenor is required")
  @Column(nullable = false)
  private Integer maxTenor;

  @NotNull(message = "Minimum loan amount is required")
  @Column(nullable = false)
  private BigDecimal minLoanAmount;

  @NotNull(message = "Maximum loan amount is required")
  @Column(nullable = false)
  private BigDecimal maxLoanAmount;

  @NotNull(message = "Admin fee is required")
  @Column(nullable = false)
  private BigDecimal adminFee;

  @Column(nullable = false)
  private Boolean isActive;
}
