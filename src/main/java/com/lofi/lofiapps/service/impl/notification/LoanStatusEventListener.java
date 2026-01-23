package com.lofi.lofiapps.service.impl.notification;

import com.lofi.lofiapps.model.event.LoanStatusChangedEvent;
import com.lofi.lofiapps.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for loan status changes. Notifications are sent ONLY AFTER the transaction commits
 * successfully. This ensures compliance with OJK/BI workflow rules: "No notification without
 * successful transaction"
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoanStatusEventListener {

  private final NotificationService notificationService;

  /**
   * Handle loan status change events after transaction commits. Uses AFTER_COMMIT phase to ensure
   * notifications are only sent when the database transaction is successful.
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleLoanStatusChanged(LoanStatusChangedEvent event) {
    log.info(
        "[EVENT] Loan {} status changed from {} to {} by {}",
        event.getLoanId(),
        event.getFromStatus(),
        event.getToStatus(),
        event.getActionBy());

    try {
      notificationService.notifyLoanStatusChange(event.getCustomerId(), event.getToStatus());
      log.info(
          "[EVENT] Notification sent for loan {} status: {}",
          event.getLoanId(),
          event.getToStatus());
    } catch (Exception e) {
      // Log error but don't fail - notification failure should not affect the main
      // flow
      log.error(
          "[EVENT] Failed to send notification for loan {}: {}", event.getLoanId(), e.getMessage());
    }
  }
}
