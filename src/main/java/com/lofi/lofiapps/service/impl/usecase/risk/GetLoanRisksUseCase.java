package com.lofi.lofiapps.service.impl.usecase.risk;

import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.entity.RiskCheck;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.RiskCheckService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving and performing risk checks for a loan. Replaces simulated checks with
 * persisted risk check entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GetLoanRisksUseCase {

  private final LoanRepository loanRepository;
  private final RiskCheckService riskCheckService;

  @Transactional
  public LoanRiskResponse execute(UUID loanId) {
    Loan loan =
        loanRepository
            .findById(loanId)
            .orElseThrow(() -> new ResourceNotFoundException("Loan", "id", loanId.toString()));

    // Get existing risk checks
    List<RiskCheck> existingChecks = riskCheckService.getRiskChecks(loanId);

    // If no checks exist or checks are outdated, perform new checks
    if (existingChecks.isEmpty()) {
      log.info("No existing risk checks found for loan {}, performing new checks", loanId);
      // Perform checks as system user (null for now, should be current user in
      // production)
      existingChecks = riskCheckService.performRiskChecks(loan, null);
    }

    // Convert to DTOs
    List<RiskItem> risks =
        existingChecks.stream().map(riskCheckService::toRiskItem).collect(Collectors.toList());

    // Determine if loan can be disbursed
    boolean canDisburse = riskCheckService.canDisburse(loanId);

    return LoanRiskResponse.builder().loanId(loanId).risks(risks).canDisburse(canDisburse).build();
  }
}
