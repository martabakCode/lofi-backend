package com.lofi.lofiapps.model.event;

import com.lofi.lofiapps.enums.LoanStatus;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain event published when a loan status changes. Used to trigger notifications AFTER the
 * transaction commits (OJK/BI compliant - no notification before commit).
 */
@Getter
@Builder
@AllArgsConstructor
public class LoanStatusChangedEvent {
  private final UUID loanId;
  private final UUID customerId;
  private final String customerEmail;
  private final LoanStatus fromStatus;
  private final LoanStatus toStatus;
  private final String actionBy;
  private final String notes;
}
