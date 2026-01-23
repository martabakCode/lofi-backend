package com.lofi.lofiapps.service.impl.notification;

import com.lofi.lofiapps.dto.response.EmailDraftResponse;
import com.lofi.lofiapps.dto.response.NotificationGenerationResponse;
import com.lofi.lofiapps.entity.Notification;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.NotificationRepository;
import com.lofi.lofiapps.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationGenerationUseCase notificationGenerationUseCase;
  private final EmailDraftGenerationUseCase emailDraftGenerationUseCase;

  @Override
  public void notifyForgotPassword(String email, String token) {
    log.info("[EMAIL] Password Reset Request for {}: Token: {}", email, token);
    // In a real implementation, this would send an actual email
  }

  @Override
  public void notifyPasswordResetSuccess(String email) {
    log.info("[EMAIL] Password Reset Successful for {}", email);
    // In a real implementation, this would send an actual email
  }

  // Helper method to simulate email sending if needed, but the interface methods
  // are preferred
  @Override
  public void sendEmail(String to, String subject, String body) {
    log.info("[EMAIL] To: {}, Subject: {}, Body: {}", to, subject, body);
  }

  @Override
  public void sendPushNotification(String token, String title, String message) {
    log.info("[PUSH] Token: {}, Title: {}, Message: {}", token, title, message);
  }

  @Override
  public void sendInAppNotification(
      UUID userId, String title, String message, String type, String link) {
    log.info("[IN-APP] User: {}, Title: {}, Message: {}", userId, title, message);
    Notification notification =
        Notification.builder()
            .userId(userId)
            .title(title)
            .message(message)
            .type(type)
            .link(link)
            .isRead(false)
            .build();
    notificationRepository.save(notification);
  }

  @Override
  public void notifyLoanStatusChange(UUID userId, LoanStatus newStatus) {
    // Generate AI-powered notification content
    NotificationGenerationResponse aiContent =
        notificationGenerationUseCase.execute("LOAN_STATUS_CHANGE", "CUSTOMER");

    String title = aiContent.getTitle();
    String message = aiContent.getMessage() + " [Status: " + newStatus + "]";

    sendInAppNotification(userId, title, message, "LOAN_STATUS", "/loans");

    // Email is mandatory for certain status changes, using AI for email draft
    if (newStatus == LoanStatus.APPROVED
        || newStatus == LoanStatus.REJECTED
        || newStatus == LoanStatus.CANCELLED) {

      EmailDraftResponse emailDraft = emailDraftGenerationUseCase.execute("LOAN_" + newStatus);
      sendEmail(
          "user@example.com", // In real apps, fetch user email
          emailDraft.getSubject(),
          emailDraft.getBodyHtml());
    }

    if (newStatus == LoanStatus.SUBMITTED) {
      log.info(
          "[PUSH] To: Branch Manager & Marketing, Title: New Loan Application, Message: A new loan has been submitted.");
    }
  }
}
