package com.lofi.lofiapps.service.impl.usecase.loan;

import com.lofi.lofiapps.dto.response.BranchManagerSupportResponse;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchManagerSupportUseCase {

  public BranchManagerSupportResponse execute(UUID loanId) {
    log.info("Branch manager support analysis for loan: {}", loanId);
    return BranchManagerSupportResponse.builder()
        .confidence(0.92)
        .branchRisks(Collections.emptyList())
        .attentionPoints(
            Collections.singletonList("Monitor local market trends for this customer sector"))
        .limitations(Collections.emptyList())
        .build();
  }
}
