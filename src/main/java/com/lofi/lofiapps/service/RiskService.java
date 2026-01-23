package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.ResolveRiskRequest;
import com.lofi.lofiapps.dto.response.LoanRiskResponse;
import com.lofi.lofiapps.dto.response.RiskItem;
import java.util.UUID;

public interface RiskService {
  LoanRiskResponse getRisks(UUID loanId);

  RiskItem resolveRisk(UUID riskId, ResolveRiskRequest request);
}
