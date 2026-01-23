package com.lofi.lofiapps.service.impl.loan;

import com.lofi.lofiapps.dto.response.BackOfficeRiskEvaluationResponse;
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
 * Back Office Risk Evaluation UseCase.
 *
 * <p>Per MCP Rules & Workflow Section 7.3 (Back Office Workflow - Final Authority): - Receive
 * Approved Loan → Recalculate Risk → Final Approve → Disbursement
 *
 * <p>Back Office wajib cek per Workflow: - DBR / DSR - Slip gaji - Rekening koran - Kondisi rumah -
 * Riwayat SLIK - Konsistensi data
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackOfficeRiskEvaluationUseCase {

  private final DocumentRepository documentRepository;

  /**
   * Back Office risk evaluation assistance.
   *
   * <p>Per Notification Workflow Section 4.4 & 4.5: - Final approve triggers: Customer (FCM),
   * Branch Manager (FCM), Email to Customer - Disbursement triggers: Customer (FCM), Email with
   * Amount, Reference, Date
   *
   * @param loan the loan to evaluate for risk
   * @return back office risk evaluation response with checklist and risk factors
   */
  public BackOfficeRiskEvaluationResponse execute(Loan loan) {
    log.info("Executing BackOfficeRiskEvaluationUseCase for loan: {}", loan.getId());

    double confidence = 0.90;
    String riskOverview = "Low Risk";
    List<String> keyRiskFactors = new ArrayList<>();
    List<String> verificationChecklist = new ArrayList<>();
    List<String> limitations = new ArrayList<>();

    BigDecimal amount = loan.getLoanAmount();
    Integer tenor = loan.getTenor();
    BigDecimal income = BigDecimal.ZERO;
    if (loan.getCustomer() != null
        && loan.getCustomer().getUserBiodata() != null
        && loan.getCustomer().getUserBiodata().getMonthlyIncome() != null) {
      income = loan.getCustomer().getUserBiodata().getMonthlyIncome();
    }

    // DBR calculation per Workflow Section 9.2 (DBR maksimal: 30–35%)
    if (income.compareTo(BigDecimal.ZERO) > 0 && amount != null && tenor != null && tenor > 0) {
      try {
        BigDecimal installment = amount.divide(BigDecimal.valueOf(tenor), MathContext.DECIMAL32);
        BigDecimal dbr = installment.divide(income, MathContext.DECIMAL32);

        if (dbr.compareTo(new BigDecimal("0.35")) > 0) {
          riskOverview = "Medium-High Risk (DBR > 35%)";
          keyRiskFactors.add(
              "High DBR: " + String.format("%.2f", dbr.doubleValue() * 100) + "% (Limit: 35%)");
          confidence -= 0.15;
          verificationChecklist.add(
              "Deep dive into bank statement to find undeclared liabilities.");
        }
      } catch (Exception e) {
        log.warn("DBR Calc error", e);
      }
    } else {
      keyRiskFactors.add("Income is Missing/Zero or Loan details invalid");
      riskOverview = "High Risk (Missing Data)";
      confidence -= 0.3;
    }

    // Document verification per Workflow Section 6.2 & 7.3
    List<Document> docs = documentRepository.findByLoanId(loan.getId());
    boolean hasPayslip = docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.PAYSLIP);
    boolean hasBankStatement =
        docs.stream().anyMatch(d -> d.getDocumentType() == DocumentType.BANK_STATEMENT);

    if (!hasPayslip) {
      keyRiskFactors.add("Missing Salary Slip");
      verificationChecklist.add("Request physical Salary Slip or HR letter.");
      confidence -= 0.1;
    } else {
      verificationChecklist.add("Verify Salary Slip consistency with mutation.");
    }

    if (!hasBankStatement) {
      keyRiskFactors.add("Missing Bank Statement");
      verificationChecklist.add("Request 3 months Bank Statement.");
      confidence -= 0.1;
    } else {
      verificationChecklist.add("Analyze Bank Statement pattern for cashflow stability.");
    }

    // Housing verification per Workflow Section 4.2.B (Domisili & Kontak)
    if (loan.getCustomer() != null && loan.getCustomer().getUserBiodata() != null) {
      String city = loan.getCustomer().getUserBiodata().getCity();
      if (city != null) {
        if ("Jakarta".equalsIgnoreCase(city)
            || "Surabaya".equalsIgnoreCase(city)
            || "Bandung".equalsIgnoreCase(city)) {
          verificationChecklist.add(
              "Housing: High cost area (" + city + "). Verify status (Own/Rent/Family).");
        } else {
          verificationChecklist.add("Housing: Verify logic of domicile vs workplace.");
        }
      }
    }

    // Multiple risk factors upgrade
    if (keyRiskFactors.size() > 2) {
      riskOverview = "High Risk (Multiple Factors)";
    }

    return BackOfficeRiskEvaluationResponse.builder()
        .confidence(confidence)
        .riskOverview(riskOverview)
        .keyRiskFactors(keyRiskFactors)
        .verificationChecklist(verificationChecklist)
        .limitations(limitations)
        .build();
  }
}
