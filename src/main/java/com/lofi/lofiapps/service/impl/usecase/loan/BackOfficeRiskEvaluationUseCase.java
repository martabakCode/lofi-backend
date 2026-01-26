package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.BackOfficeRiskEvaluationResponse;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackOfficeRiskEvaluationUseCase {

  public BackOfficeRiskEvaluationResponse execute(UUID loanId) {
    log.info("Back office risk evaluation for loan: {}", loanId);
    return BackOfficeRiskEvaluationResponse.builder()
        .confidence(0.88)
        .riskOverview("Initial risk evaluation completed for loan " + loanId)
        .keyRiskFactors(Collections.emptyList())
        .verificationChecklist(Collections.singletonList("Verify employment documents"))
        .limitations(Collections.emptyList())
        .build();
  }
}
