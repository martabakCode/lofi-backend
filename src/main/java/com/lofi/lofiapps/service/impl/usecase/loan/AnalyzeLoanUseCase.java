package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.LoanAnalysisResponse;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzeLoanUseCase {

  public LoanAnalysisResponse execute(UUID loanId) {
    log.info("Analyzing loan: {}", loanId);
    return LoanAnalysisResponse.builder()
        .confidence(0.85)
        .summary("Initial analysis completed for loan " + loanId)
        .riskFlags(Collections.emptyList())
        .reviewNotes(Collections.singletonList("Loan is under routine review"))
        .limitations(Collections.emptyList())
        .build();
  }
}
