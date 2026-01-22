package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.mapper.LoanDtoMapper;
import com.lofi.lofiapps.model.dto.request.LoanRequest;
import com.lofi.lofiapps.model.dto.response.LoanResponse;
import com.lofi.lofiapps.model.entity.Loan;
import com.lofi.lofiapps.model.entity.Product;
import com.lofi.lofiapps.model.entity.User;
import com.lofi.lofiapps.model.enums.ApprovalStage;
import com.lofi.lofiapps.model.enums.LoanStatus;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.repository.ProductRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplyLoanUseCase {
  private final LoanRepository loanRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final LoanDtoMapper loanDtoMapper;

  @Transactional
  public LoanResponse execute(LoanRequest request, UUID customerId) {
    Product product =
        productRepository
            .findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

    User customer =
        userRepository
            .findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    // Validation is done against product constraints below

    if (request.getLoanAmount().compareTo(product.getMinLoanAmount()) < 0
        || request.getLoanAmount().compareTo(product.getMaxLoanAmount()) > 0) {
      throw new IllegalArgumentException(
          "Loan amount must be between "
              + product.getMinLoanAmount()
              + " and "
              + product.getMaxLoanAmount());
    }

    if (request.getTenor() < product.getMinTenor() || request.getTenor() > product.getMaxTenor()) {
      throw new IllegalArgumentException(
          "Tenor must be between "
              + product.getMinTenor()
              + " and "
              + product.getMaxTenor()
              + " months");
    }

    Loan loan =
        Loan.builder()
            .customer(customer)
            .product(product)
            .loanAmount(request.getLoanAmount())
            .tenor(request.getTenor())
            .loanStatus(LoanStatus.DRAFT) // Starts as Draft
            .currentStage(ApprovalStage.CUSTOMER)
            .lastStatusChangedAt(LocalDateTime.now())
            .build();

    Loan savedLoan = loanRepository.save(loan);
    return loanDtoMapper.toResponse(savedLoan);
  }
}
