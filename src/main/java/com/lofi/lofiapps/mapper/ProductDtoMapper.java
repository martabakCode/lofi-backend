package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.dto.request.CreateProductRequest;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductDtoMapper {

  public Product toDomain(CreateProductRequest request) {
    if (request == null) return null;
    return Product.builder()
        .productCode(request.getProductCode())
        .productName(request.getProductName())
        .description(request.getDescription())
        .interestRate(request.getInterestRate())
        .adminFee(request.getAdminFee())
        .minTenor(request.getMinTenor())
        .maxTenor(request.getMaxTenor())
        .minLoanAmount(request.getMinLoanAmount())
        .maxLoanAmount(request.getMaxLoanAmount())
        .isActive(true)
        .build();
  }

  public ProductResponse toResponse(Product domain) {
    if (domain == null) return null;
    return ProductResponse.builder()
        .id(domain.getId())
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
