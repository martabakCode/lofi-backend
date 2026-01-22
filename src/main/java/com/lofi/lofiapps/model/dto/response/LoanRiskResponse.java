package com.lofi.lofiapps.model.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanRiskResponse {
  private UUID loanId;
  private List<RiskItem> risks;
  private boolean canDisburse;
}
