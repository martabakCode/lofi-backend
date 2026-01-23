package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.LoanStatus;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanCriteria {
  private LoanStatus status;
  private UUID branchId;
  private UUID customerId;
}
