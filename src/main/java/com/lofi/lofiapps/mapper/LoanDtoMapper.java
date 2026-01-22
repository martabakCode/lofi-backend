package com.lofi.lofiapps.mapper;

import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanDtoMapper {
  private final ProductDtoMapper productDtoMapper;

  public LoanResponse toResponse(Loan domain) {
    if (domain == null) return null;
    return LoanResponse.builder()
        .id(domain.getId())
        .customerId(domain.getCustomer() != null ? domain.getCustomer().getId() : null)
        .customerName(domain.getCustomer() != null ? domain.getCustomer().getFullName() : null)
        .product(productDtoMapper.toResponse(domain.getProduct()))
        .loanAmount(domain.getLoanAmount())
        .tenor(domain.getTenor())
        .loanStatus(domain.getLoanStatus())
        .currentStage(domain.getCurrentStage())
        .submittedAt(domain.getSubmittedAt())
        .approvedAt(domain.getApprovedAt())
        .rejectedAt(domain.getRejectedAt())
        .build();
  }
}
