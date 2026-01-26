package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import com.lofi.lofiapps.service.RiskService;
import com.lofi.lofiapps.service.impl.usecase.risk.GetLoanRisksUseCase;
import com.lofi.lofiapps.service.impl.usecase.risk.ResolveRiskUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskServiceImpl implements RiskService {

  private final GetLoanRisksUseCase getLoanRisksUseCase;
  private final ResolveRiskUseCase resolveRiskUseCase;

  @Override
  @Transactional(readOnly = true)
  public LoanRiskResponse getRisks(UUID loanId) {
    return getLoanRisksUseCase.execute(loanId);
  }

  @Override
  @Transactional
  public RiskItem resolveRisk(UUID riskId, ResolveRiskRequest request) {
    return resolveRiskUseCase.execute(riskId, request);
  }
}
