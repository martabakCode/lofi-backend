package com.lofi.lofiapps.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;
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
public class JpaProduct extends JpaBaseEntity {

  @NotBlank(message = "Product Code is required")
  @Column(nullable = false, unique = true)
  private String productCode;

  @NotBlank(message = "Product Name is required")
  @Column(nullable = false)
  private String productName;

  private String description;

  @NotNull(message = "Interest Rate is required")
  @Positive
  @Column(nullable = false)
  private BigDecimal interestRate;

  @NotNull(message = "Admin Fee is required")
  @Positive
  @Column(nullable = false)
  private BigDecimal adminFee;

  @NotNull(message = "Min Tenor is required")
  @Positive
  @Column(nullable = false)
  private Integer minTenor;

  @NotNull(message = "Max Tenor is required")
  @Positive
  @Column(nullable = false)
  private Integer maxTenor;

  @NotNull(message = "Min Loan Amount is required")
  @Positive
  @Column(nullable = false)
  private BigDecimal minLoanAmount;

  @NotNull(message = "Max Loan Amount is required")
  @Positive
  @Column(nullable = false)
  private BigDecimal maxLoanAmount;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isActive = true;
}
