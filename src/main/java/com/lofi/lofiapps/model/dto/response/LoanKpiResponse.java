package com.lofi.lofiapps.model.dto.response;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanKpiResponse {
  private long totalLoans;
  private long totalSubmitted;
  private long totalReviewed;
  private long totalApproved;
  private long totalRejected;
  private long totalCancelled;
  private long totalDisbursed;
  private long totalCompleted;
  private Map<String, Long> loansByProduct;
}
