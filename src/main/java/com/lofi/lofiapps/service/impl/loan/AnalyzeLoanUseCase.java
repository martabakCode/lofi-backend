package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.dto.response.LoanAnalysisResponse;
import com.lofi.lofiapps.entity.Loan;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Loan Analyzer UseCase.
 *
 * <p>Per MCP Rules (AI MCP Position in Workflow): - AI hanya membantu analisis - AI memberi
 * rekomendasi - AI tidak mengambil keputusan
 *
 * <p>Workflow Reference: Section 11 - AI Analysis (Optional) → Staff Review
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeLoanUseCase {

  /**
   * Analyze loan application for risk factors and recommendations.
   *
   * <p>Per Workflow Section 10 - Failure & Block Conditions: - DBR > limit → REJECT - Data tidak
   * lengkap → BLOCK
   *
   * @param loan the loan to analyze
   * @return analysis response with confidence, risk flags, and recommendations
   */
  public LoanAnalysisResponse execute(Loan loan) {
    log.info("Executing AnalyzeLoanUseCase for loan: {}", loan.getId());

    List<String> riskFlags = new ArrayList<>();
    List<String> reviewNotes = new ArrayList<>();
    List<String> limitations = new ArrayList<>();

    BigDecimal amount = loan.getLoanAmount();
    Integer tenor = loan.getTenor();
    BigDecimal income =
        (loan.getCustomer() != null && loan.getCustomer().getUserBiodata() != null)
            ? loan.getCustomer().getUserBiodata().getMonthlyIncome()
            : null;

    double confidence = 0.88;
    String summary =
        "Loan application looks stable with some attention needed on DBR and Documents.";

    // High amount risk check
    if (amount != null && amount.compareTo(new BigDecimal("100000000")) > 0) {
      riskFlags.add("High Loan Amount (> 100jt)");
      confidence -= 0.05;
    }

    // Long tenor risk check
    if (tenor != null && tenor > 24) {
      riskFlags.add("Long Tenor Risk");
    }

    // DBR calculation per Workflow Section 9.2 (DBR maksimal: 30–35%)
    if (income != null && income.compareTo(BigDecimal.ZERO) > 0 && tenor != null && tenor > 0) {
      try {
        BigDecimal installment = amount.divide(BigDecimal.valueOf(tenor), MathContext.DECIMAL32);
        BigDecimal dbr = installment.divide(income, MathContext.DECIMAL32);

        if (dbr.compareTo(new BigDecimal("0.35")) > 0) {
          riskFlags.add("High DBR (>" + String.format("%.2f", dbr.doubleValue() * 100) + "%)");
          summary = "CAUTION: DBR exceeds regulatory limit of 35%.";
          reviewNotes.add("Verify other income sources");
          confidence -= 0.2;
        }
      } catch (Exception e) {
        log.warn("Error calculating DBR", e);
      }
    } else {
      riskFlags.add("Income Data Missing or Invalid");
      confidence -= 0.3;
    }

    reviewNotes.add("Check recent mutation");
    reviewNotes.add("Verify employer phone number");

    limitations.add("No credit bureau connection");
    limitations.add("Self-declared income only");

    return LoanAnalysisResponse.builder()
        .confidence(confidence)
        .summary(summary)
        .riskFlags(riskFlags)
        .reviewNotes(reviewNotes)
        .limitations(limitations)
        .build();
  }
}
