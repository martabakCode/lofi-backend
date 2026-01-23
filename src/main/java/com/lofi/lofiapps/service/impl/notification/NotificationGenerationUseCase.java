package com.lofi.lofiapps.service.impl.notification;

import com.lofi.lofiapps.dto.response.NotificationGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Notification Generation UseCase.
 *
 * <p>Per Notification Workflow Section 1 - Notification Channels: - Firebase Push: Real-time
 * workflow notification - Email: Legal & formal communication - In-App: Status visibility - Audit
 * Log: Traceability
 *
 * <p>Per Section 2 - Global Notification Rules (Hard Rules): - State change → notification
 * mandatory - No notification without successful transaction - Notification content non-sensitive -
 * Email for legal-impact events - Retry & fallback supported
 *
 * <p>Per Section 6 - Firebase Notification Payload (Standard): - type, loanId, status, message,
 * timestamp - Tidak kirim nominal sensitif - Gunakan loanId sebagai context
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationGenerationUseCase {

  /**
   * Generate AI-powered notification content.
   *
   * <p>Per Notification Workflow Section 11 - Audit Logging (Mandatory): Setiap notifikasi:
   * eventType, recipientRole, channel, timestamp, status
   *
   * <p>Per Section 12 - Pen-Test Safety Rules: - No PII in push notification
   *
   * @param eventType the type of event triggering the notification
   * @param recipientRole the role of the recipient
   * @return notification generation response with title and message
   */
  public NotificationGenerationResponse execute(String eventType, String recipientRole) {
    log.info(
        "Executing NotificationGenerationUseCase for event: {}, role: {}",
        eventType,
        recipientRole);

    // Per Section 6 - Non-sensitive notification content
    return NotificationGenerationResponse.builder()
        .title("Notification: " + eventType)
        .message("You have a new update regarding your loan activity for role " + recipientRole)
        .build();
  }
}
