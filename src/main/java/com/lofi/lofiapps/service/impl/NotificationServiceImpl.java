package com.lofi.lofiapps.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.lofi.lofiapps.entity.Notification;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.repository.NotificationRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final JavaMailSender javaMailSender;
  private final FirebaseMessaging firebaseMessaging;
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Override
  public void sendEmail(String to, String subject, String body) {
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      // Ensure we have a valid from address, falling back if property is not set or
      // empty
      String from =
          (senderEmail != null && !senderEmail.isEmpty()) ? senderEmail : "noreply@lofi.com";
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(body);
      javaMailSender.send(message);
      log.info("Email sent successfully to {}", to);
    } catch (Exception e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      // We don't throw exception here to avoid blocking main flow if notification
      // fails
    }
  }

  @Override
  public void sendPushNotification(String token, String title, String body) {
    if (token == null || token.isEmpty()) {
      log.warn("Cannot send push notification: Token is null or empty");
      return;
    }

    try {
      com.google.firebase.messaging.Notification notification =
          com.google.firebase.messaging.Notification.builder()
              .setTitle(title)
              .setBody(body)
              .build();

      Message message = Message.builder().setToken(token).setNotification(notification).build();

      String response = firebaseMessaging.send(message);
      log.info("Push notification sent successfully via Firebase: {}", response);
    } catch (Exception e) {
      log.error("Failed to send push notification to token {}: {}", token, e.getMessage());
    }
  }

  @Override
  @Transactional
  public void sendInAppNotification(
      UUID userId,
      String title,
      String body,
      com.lofi.lofiapps.enums.NotificationType type,
      UUID referenceId,
      String link) {
    try {
      Notification notification =
          Notification.builder()
              .userId(userId)
              .title(title)
              .body(body)
              .type(type)
              .referenceId(referenceId)
              .link(link)
              .isRead(false)
              .build();

      notificationRepository.save(notification);
      log.info("In-app notification saved for user {}", userId);
    } catch (Exception e) {
      log.error("Failed to save in-app notification for user {}: {}", userId, e.getMessage());
    }
  }

  @Override
  @Transactional
  public void notifyLoanStatusChange(UUID userId, LoanStatus newStatus) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      log.warn("User not found for ID: {}, cannot send notification", userId);
      return;
    }

    String title = "Loan Status Update";
    String message = "Your loan status has been updated to: " + newStatus;

    // Customize message based on status (simple logic)
    switch (newStatus) {
      case SUBMITTED:
        message = "Your loan application has been submitted successfully.";
        break;
      case APPROVED:
        message = "Congratulations! Your loan application has been approved.";
        break;
      case REJECTED:
        message = "We regret to inform you that your loan application has been rejected.";
        break;
      case DISBURSED:
        message = "Funds have been disbursed to your account. Please check your balance.";
        break;
      case COMPLETED:
        message = "Thank you! Your loan has been fully paid.";
        break;
      default:
        break;
    }

    // 1. In-App Notification
    sendInAppNotification(
        userId,
        title,
        message,
        com.lofi.lofiapps.enums.NotificationType.LOAN,
        null,
        null); // TODO: Pass Loan ID if available

    // 2. Push Notification
    if (user.getFirebaseToken() != null && !user.getFirebaseToken().isEmpty()) {
      sendPushNotification(user.getFirebaseToken(), title, message);
    }

    // 3. Email Notification
    sendEmail(user.getEmail(), title, message);
  }

  @Override
  public void notifyForgotPassword(String email, String token) {
    String subject = "Password Reset Request";
    String body =
        "You have requested to reset your password. Use the following token: "
            + token
            + "\n\nIf you did not request this, please ignore this email.";

    sendEmail(email, subject, body);
  }

  @Override
  public void notifyPasswordResetSuccess(String email) {
    String subject = "Password Reset Successful";
    String body =
        "Your password has been successfully reset. You can now login with your new password.";

    sendEmail(email, subject, body);
  }

  @Override
  public java.util.List<com.lofi.lofiapps.dto.response.NotificationResponse> getNotifications(
      UUID userId) {
    return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(
            notification ->
                com.lofi.lofiapps.dto.response.NotificationResponse.builder()
                    .id(notification.getId())
                    .userId(notification.getUserId())
                    .title(notification.getTitle())
                    .body(notification.getBody())
                    .type(notification.getType())
                    .referenceId(notification.getReferenceId())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .link(notification.getLink())
                    .build())
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  @Transactional
  public void markAsRead(UUID id) {
    Notification notification = notificationRepository.findById(id).orElse(null);
    if (notification != null) {
      notification.setIsRead(true);
      notificationRepository.save(notification);
    }
  }
}
