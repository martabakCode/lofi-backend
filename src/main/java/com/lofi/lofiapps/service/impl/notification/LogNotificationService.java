package com.lofi.lofiapps.service.impl.notification;

import com.lofi.lofiapps.model.entity.Notification;
import com.lofi.lofiapps.model.enums.LoanStatus;
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
    String title = "Loan Status Update";
    String message = "Your loan status has been updated to: " + newStatus;
    sendInAppNotification(userId, title, message, "LOAN_STATUS", "/loans");

    // Email is mandatory for certain status changes
    if (newStatus == LoanStatus.APPROVED
        || newStatus == LoanStatus.REJECTED
        || newStatus == LoanStatus.CANCELLED) {
      sendEmail("user@example.com", title, message); // Should get actual user email
    }
  }
}
