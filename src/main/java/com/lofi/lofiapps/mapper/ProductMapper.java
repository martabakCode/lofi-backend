package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.entity.JpaProduct;
import com.lofi.lofiapps.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
  public Product toDomain(JpaProduct entity) {
    if (entity == null) return null;
    return Product.builder()
        .id(entity.getId())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy())
        .lastModifiedBy(entity.getLastModifiedBy())
        .deletedAt(entity.getDeletedAt())
        .productCode(entity.getProductCode())
        .productName(entity.getProductName())
        .description(entity.getDescription())
        .interestRate(entity.getInterestRate())
        .adminFee(entity.getAdminFee())
        .minTenor(entity.getMinTenor())
        .maxTenor(entity.getMaxTenor())
        .minLoanAmount(entity.getMinLoanAmount())
        .maxLoanAmount(entity.getMaxLoanAmount())
        .isActive(entity.getIsActive())
        .build();
  }

  public JpaProduct toJpa(Product domain) {
    if (domain == null) return null;
    return JpaProduct.builder()
        .id(domain.getId())
        .createdAt(domain.getCreatedAt())
        .updatedAt(domain.getUpdatedAt())
        .createdBy(domain.getCreatedBy())
        .lastModifiedBy(domain.getLastModifiedBy())
        .deletedAt(domain.getDeletedAt())
        .productCode(domain.getProductCode())
        .productName(domain.getProductName())
        .description(domain.getDescription())
        .interestRate(domain.getInterestRate())
        .adminFee(domain.getAdminFee())
        .minTenor(domain.getMinTenor())
        .maxTenor(domain.getMaxTenor())
        .minLoanAmount(domain.getMinLoanAmount())
        .maxLoanAmount(domain.getMaxLoanAmount())
        .isActive(domain.getIsActive())
        .build();
  }
}
