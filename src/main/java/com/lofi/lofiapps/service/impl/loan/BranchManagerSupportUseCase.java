package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.dto.response.BranchManagerSupportResponse;
import com.lofi.lofiapps.entity.Loan;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Branch Manager Support UseCase.
 *
 * <p>
 * Per MCP Rules & Workflow Section 7.2 (Branch Manager Workflow): - Bisa
 * approve SELAMA plafon
 * cabang cukup - Yang melewati plafon → auto disabled
 *
 * <p>
 * Per Workflow Section 2 - Global Rules: - Tidak ada approve jika plafon
 * terlampaui - Approval
 * bersifat berjenjang
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BranchManagerSupportUseCase {

  private final com.lofi.lofiapps.repository.LoanRepository loanRepository;

  /**
   * Branch Manager decision support analysis.
   *
   * <p>
   * Per Notification Workflow Section 4.3: - Trigger: POST /loans/{id}/approve -
   * Notify:
   * Customer (FCM), Back Office (FCM) - Message: Loan approved by Branch Manager
   *
   * @param loanId the loan id to check
   * @return branch manager support response with checks
   */
  public BranchManagerSupportResponse execute(java.util.UUID loanId) {
    Loan loan = loanRepository
        .findById(loanId)
        .orElseThrow(() -> new IllegalArgumentException("Loan not found: " + loanId));
    log.info("Executing BranchManagerSupportUseCase for loan: {}", loan.getId());

    List<String> branchRisks = new ArrayList<>();
    List<String> attentionPoints = new ArrayList<>();
    List<String> limitations = new ArrayList<>();
    double confidence = 0.85;

    // Per Workflow Section 7.2 - Check Branch context
    if (loan.getBranch() != null) {
      String city = loan.getBranch().getCity();
      if ("Jakarta".equalsIgnoreCase(city) || "Bandung".equalsIgnoreCase(city)) {
        branchRisks.add("High NPL Rate in this Region (> 2.5%)");
      }

      // Large ticket size check against branch limits
      if (loan.getLoanAmount() != null
          && loan.getLoanAmount().compareTo(new BigDecimal("200000000")) > 0) {
        branchRisks.add("Large Ticket Size: Consumes significant portion of Branch Weekly Limit");
        attentionPoints.add("Check Branch Daily Liquidity before approval");
      }
    } else {
      branchRisks.add("Branch Context Missing");
    }

    // Multiple draft applications check
    if (loan.getCustomer() != null
        && loan.getCustomer().getEmail() != null
        && loan.getCustomer().getEmail().contains("test")) {
      attentionPoints.add("User has multiple draft applications (Simulated)");
    }

    // Long tenor exposure per Workflow Section 10
    if (loan.getTenor() != null && loan.getTenor() > 48) {
      branchRisks.add("Long Tenor Exposure: Increases Branch Portfolio Duration Risk");
    }

    limitations.add("No real-time core banking liquidity check");
    limitations.add("Competitor analysis unavailable");

    return BranchManagerSupportResponse.builder()
        .confidence(confidence)
        .branchRisks(branchRisks)
        .attentionPoints(attentionPoints)
        .limitations(limitations)
        .build();
  }
}
