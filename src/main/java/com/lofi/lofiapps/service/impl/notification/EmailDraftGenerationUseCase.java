package com.lofi.lofiapps.service.impl.notification;

import com.lofi.lofiapps.dto.response.EmailDraftResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email Draft Generation UseCase.
 *
 * <p>Per Notification Workflow Section 7 - Email Rules (Compliance):
 *
 * <p>Email WAJIB untuk: - Reset Password - Final Approval - Disbursement
 *
 * <p>Email TIDAK BOLEH: - Berisi token mentah (kecuali reset) - Menampilkan data sensitif
 *
 * <p>Per Section 4.4 - Back Office Final Approve: - Email Subject: Loan Approval Confirmation
 *
 * <p>Per Section 4.5 - Back Office Disbursement: - Email Content: Amount, Reference number, Date
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailDraftGenerationUseCase {

  /**
   * Generate email draft content.
   *
   * <p>Per Notification Workflow Section 12 - Pen-Test Safety Rules: - No PII in content - Deep
   * link token validated backend - Rate limit for password reset
   *
   * @param eventType the type of event triggering the email
   * @return email draft response with subject, body, and disclaimer
   */
  public EmailDraftResponse execute(String eventType) {
    log.info("Executing EmailDraftGenerationUseCase for event: {}", eventType);

    // Per Workflow Section 7 - Generate compliant email content
    return EmailDraftResponse.builder()
        .subject("Loan Update: " + eventType)
        .bodyHtml("<p>Your loan status has been updated to <b>" + eventType + "</b>.</p>")
        .disclaimer("This is an AI generated draft for review.")
        .build();
  }
}
