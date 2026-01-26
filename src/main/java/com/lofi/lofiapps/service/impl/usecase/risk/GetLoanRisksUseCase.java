package com.lofi.lofiapps.service.impl.usecase.risk;

import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.LoanRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetLoanRisksUseCase {
  private final LoanRepository loanRepository;

  public LoanRiskResponse execute(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    List<RiskItem> risks = new ArrayList<>();

    // Simulating some risk checks
    risks.add(
        RiskItem.builder()
            .id("CHECK_001")
            .name("Blacklist Check")
            .description("Check if customer is in national blacklist")
            .status(RiskItem.RiskStatus.PASS)
            .build());

    risks.add(
        RiskItem.builder()
            .id("CHECK_002")
            .name("Credit Score")
            .description("Automated credit scoring")
            .status(
                loan.getLoanAmount().doubleValue() > 50000000
                    ? RiskItem.RiskStatus.WARNING
                    : RiskItem.RiskStatus.PASS)
            .comments(
                loan.getLoanAmount().doubleValue() > 50000000
                    ? "High loan amount requires further review"
                    : null)
            .build());

    risks.add(
        RiskItem.builder()
            .id("CHECK_003")
            .name("Document Clarity")
            .description("Verify uploaded document images are readable")
            .status(RiskItem.RiskStatus.PASS)
            .build());

    boolean canDisburse = risks.stream().noneMatch(r -> r.getStatus() == RiskItem.RiskStatus.FAIL);

    return LoanRiskResponse.builder().loanId(loanId).risks(risks).canDisburse(canDisburse).build();
  }
}
