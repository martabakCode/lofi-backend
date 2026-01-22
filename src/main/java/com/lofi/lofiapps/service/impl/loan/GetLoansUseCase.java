package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.model.dto.request.LoanCriteria;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.dto.response.PagedResponse;
import com.lofi.lofiapps.model.dto.response.ProductResponse;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.repository.LoanRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLoansUseCase {
  private final LoanRepository loanRepository;

  public PagedResponse<LoanResponse> execute(LoanCriteria criteria, Pageable pageable) {
    Page<Loan> page = loanRepository.findAll(criteria, pageable);

    List<LoanResponse> items =
        page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  private LoanResponse mapToResponse(Loan loan) {
    return LoanResponse.builder()
        .id(loan.getId())
        .customerId(loan.getCustomer() != null ? loan.getCustomer().getId() : null)
        .customerName(loan.getCustomer() != null ? loan.getCustomer().getFullName() : null)
        .product(
            loan.getProduct() != null
                ? ProductResponse.builder()
                    .id(loan.getProduct().getId())
                    .productCode(loan.getProduct().getProductCode())
                    .productName(loan.getProduct().getProductName())
                    .interestRate(loan.getProduct().getInterestRate())
                    .build()
                : null)
        .loanAmount(loan.getLoanAmount())
        .tenor(loan.getTenor())
        .loanStatus(loan.getLoanStatus())
        .currentStage(loan.getCurrentStage())
        .submittedAt(loan.getSubmittedAt())
        .approvedAt(loan.getApprovedAt())
        .rejectedAt(loan.getRejectedAt())
        .build();
  }
}
