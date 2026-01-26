package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.MarketingLoanReviewResponse;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketingReviewLoanUseCase {

  public MarketingLoanReviewResponse execute(UUID loanId) {
    log.info("Marketing review for loan: {}", loanId);
    return MarketingLoanReviewResponse.builder()
        .confidence(0.9)
        .dataInconsistencies(Collections.emptyList())
        .suggestedQuestions(Collections.singletonList("Ask about source of income stability"))
        .notes("Initial marketing analysis completed for loan " + loanId)
        .build();
  }
}
