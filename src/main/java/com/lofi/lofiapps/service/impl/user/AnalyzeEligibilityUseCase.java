package com.lofi.lofiapps.service.impl.user;

import com.lofi.lofiapps.dto.response.EligibilityAnalysisResponse;
import com.lofi.lofiapps.entity.User;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Analyze Eligibility UseCase.
 *
 * <p>Per MCP Rules & Workflow Section 4.2 (Profile Completion - MANDATORY): - Data Identitas: NIK,
 * Nama lengkap, Nama gadis ibu kandung, Status perkawinan, Kontak darurat - Jika tidak lengkap →
 * Tidak bisa lanjut
 *
 * <p>Per Workflow Section 4.3 (Eligibility Check - System Rule): - IF profile incomplete → BLOCK -
 * IF exceeds DBR (30–35%) → BLOCK - IF SLIK OJK bermasalah → BLOCK
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeEligibilityUseCase {

  /**
   * Analyze user eligibility for loan products.
   *
   * <p>Per Workflow Section 4.2: - Checks Data Identitas (Wajib – OJK) - Checks Pekerjaan &
   * Keuangan (Wajib)
   *
   * @param user the user to analyze eligibility for
   * @return eligibility analysis response with missing data and potential issues
   */
  public EligibilityAnalysisResponse execute(User user) {
    log.info("Executing AnalyzeEligibilityUseCase for user: {}", user.getId());

    List<String> missingData = new ArrayList<>();
    List<String> potentialIssues = new ArrayList<>();
    List<String> notes = new ArrayList<>();
    double confidence = 0.95;

    // Per Workflow Section 4.2.A - Data Identitas (Wajib – OJK)
    if (user.getUserBiodata() == null) {
      missingData.add("Complete Biodata is missing");
      confidence = 0.0;
    } else {
      var bio = user.getUserBiodata();

      // NIK (KTP) check
      if (bio.getNik() == null || bio.getNik().isEmpty()) {
        missingData.add("NIK");
      }

      // Nama lengkap check
      if (user.getFullName() == null || user.getFullName().isEmpty()) {
        missingData.add("Full Name");
      }

      // Kontak check per Section 4.2.B
      if (bio.getPhoneNumber() == null || bio.getPhoneNumber().isEmpty()) {
        missingData.add("Contact (Phone Number)");
      }

      // Per Workflow Section 4.2.C - Pekerjaan & Keuangan (Wajib)
      if (bio.getMonthlyIncome() == null) {
        missingData.add("Monthly Income");
      } else if (bio.getMonthlyIncome().compareTo(new BigDecimal("2000000")) < 0) {
        potentialIssues.add("Income might be too low for standard products");
      }

      if (bio.getOccupation() == null) {
        missingData.add("Occupation");
      }
    }

    // DBR calculation capability check per Section 4.3
    if (user.getUserBiodata() != null && user.getUserBiodata().getMonthlyIncome() != null) {
      notes.add("Income data present. DBR can be calculated upon loan application.");
    } else {
      notes.add("Cannot calculate DBR: Income missing.");
    }

    return EligibilityAnalysisResponse.builder()
        .confidence(confidence)
        .missingData(missingData)
        .potentialIssues(potentialIssues)
        .notes(notes)
        .build();
  }
}
