package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.LoanStatus;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanCriteria {
  private LoanStatus status;
  private UUID branchId;
  private UUID customerId;

  /**
   * List of statuses to exclude from the query. Used for filtering out DRAFT and CANCELLED loans
   * from active loans view.
   */
  private List<LoanStatus> excludeStatuses;
}
