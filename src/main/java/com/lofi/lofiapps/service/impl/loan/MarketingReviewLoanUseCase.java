package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.dto.response.MarketingLoanReviewResponse;
import com.lofi.lofiapps.entity.Document;
import com.lofi.lofiapps.entity.Loan;
import com.lofi.lofiapps.enums.DocumentType;
import com.lofi.lofiapps.repository.DocumentRepository;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Marketing Review Loan UseCase.
 *
 * <p>Per MCP Rules & Workflow Section 7.1 (Marketing Workflow): - Marketing dapat Review Profile -
 * Marketing dapat View Scoring - Marketing TIDAK bisa approve
 *
 * <p>Per Workflow Section 6.2 - Conditional Document Rule (Product-Based)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingReviewLoanUseCase {

  private final DocumentRepository documentRepository;

  /**
   * Marketing review assistance for loan applications.
   *
   * <p>Per Notification Workflow Section 4.2: - Trigger: POST /loans/{id}/review - Notify: Customer
   * (FCM), Branch Manager (FCM) - Message: Your loan is under review by marketing
   *
   * @param loan the loan to review
   * @return marketing review response with inconsistencies and suggested questions
   */
  public MarketingLoanReviewResponse execute(Loan loan) {
    log.info("Executing MarketingReviewLoanUseCase for loan: {}", loan.getId());

    List<String> inconsistencies = new ArrayList<>();
    List<String> questions = new ArrayList<>();
    String notes = "Marketing Review Completed.";
    double confidence = 0.9;

    BigDecimal amount = loan.getLoanAmount();
    BigDecimal income = BigDecimal.ZERO;
    if (loan.getCustomer() != null
        && loan.getCustomer().getUserBiodata() != null
        && loan.getCustomer().getUserBiodata().getMonthlyIncome() != null) {
      income = loan.getCustomer().getUserBiodata().getMonthlyIncome();
    }

    List<Document> docs = documentRepository.findByLoanId(loan.getId());
    boolean hasIncomeProof =
        docs.stream()
            .anyMatch(
                d ->
                    d.getDocumentType() == DocumentType.PAYSLIP
                        || d.getDocumentType() == DocumentType.BANK_STATEMENT);

    // Per Workflow Section 6.2 - Document requirements
    if (!hasIncomeProof) {
      inconsistencies.add("No explicit income proof (Payslip/Bank Statement) uploaded.");
      questions.add("Can you upload a recent payslip or bank statement?");
      confidence -= 0.1;
    }

    // Large loan document requirement
    if (amount != null && amount.compareTo(new BigDecimal("50000000")) > 0) {
      if (docs.size() < 2) {
        inconsistencies.add("Loan > 50jt requires at least 2 documents.");
      }
    }

    // Income to loan ratio check
    if (income.compareTo(BigDecimal.ZERO) > 0 && amount != null) {
      BigDecimal ratio = amount.divide(income, MathContext.DECIMAL32);
      if (ratio.compareTo(new BigDecimal("20")) > 0) {
        inconsistencies.add(
            "Loan Amount is > 20x Monthly Income. Purpose might need verification.");
        questions.add("What is the detailed purpose of this loan?");
      }
    } else {
      inconsistencies.add("Income is zero or missing.");
    }

    return MarketingLoanReviewResponse.builder()
        .confidence(confidence)
        .dataInconsistencies(inconsistencies)
        .suggestedQuestions(questions)
        .notes(notes)
        .build();
  }
}
