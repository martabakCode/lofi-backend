package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.firebase.messaging.FirebaseMessaging;
import com.lofi.lofiapps.dto.response.NotificationResponse;
import com.lofi.lofiapps.entity.Notification;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.enums.LoanStatus;
import com.lofi.lofiapps.enums.NotificationType;
import com.lofi.lofiapps.repository.NotificationRepository;
import com.lofi.lofiapps.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock private JavaMailSender javaMailSender;
  @Mock private FirebaseMessaging firebaseMessaging;
  @Mock private NotificationRepository notificationRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private NotificationServiceImpl notificationService;

  private UUID userId;
  private UUID notificationId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    notificationId = UUID.randomUUID();

    user =
        User.builder()
            .id(userId)
            .email("user@example.com")
            .firebaseToken("firebase-token-123")
            .build();

    ReflectionTestUtils.setField(notificationService, "senderEmail", "noreply@lofi.com");
  }

  @Test
  @DisplayName("SendEmail should send email successfully")
  void sendEmail_ShouldSendEmailSuccessfully() {
    // Arrange
    doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

    // Act
    notificationService.sendEmail("to@example.com", "Test Subject", "Test Body");

    // Assert
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("SendEmail should handle exception gracefully")
  void sendEmail_ShouldHandleException() {
    // Arrange
    doThrow(new RuntimeException("SMTP error"))
        .when(javaMailSender)
        .send(any(SimpleMailMessage.class));

    // Act - Should not throw exception
    assertDoesNotThrow(
        () -> notificationService.sendEmail("to@example.com", "Test Subject", "Test Body"));

    // Assert
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("SendPushNotification should send notification when token exists")
  void sendPushNotification_ShouldSendWhenTokenExists() throws Exception {
    // Arrange
    when(firebaseMessaging.send(any(com.google.firebase.messaging.Message.class)))
        .thenReturn("message-id");

    // Act
    notificationService.sendPushNotification("valid-token", "Test Title", "Test Body");

    // Assert
    verify(firebaseMessaging).send(any(com.google.firebase.messaging.Message.class));
  }

  @Test
  @DisplayName("SendPushNotification should skip when token is null")
  void sendPushNotification_ShouldSkipWhenTokenNull() {
    // Act
    notificationService.sendPushNotification(null, "Test Title", "Test Body");

    // Assert
    verify(firebaseMessaging, never()).send(any());
  }

  @Test
  @DisplayName("SendPushNotification should skip when token is empty")
  void sendPushNotification_ShouldSkipWhenTokenEmpty() {
    // Act
    notificationService.sendPushNotification("", "Test Title", "Test Body");

    // Assert
    verify(firebaseMessaging, never()).send(any());
  }

  @Test
  @DisplayName("SendPushNotification should handle exception gracefully")
  void sendPushNotification_ShouldHandleException() throws Exception {
    // Arrange
    when(firebaseMessaging.send(any(com.google.firebase.messaging.Message.class)))
        .thenThrow(new RuntimeException("Firebase error"));

    // Act - Should not throw exception
    assertDoesNotThrow(
        () -> notificationService.sendPushNotification("valid-token", "Test Title", "Test Body"));

    // Assert
    verify(firebaseMessaging).send(any(com.google.firebase.messaging.Message.class));
  }

  @Test
  @DisplayName("SendInAppNotification should save notification successfully")
  void sendInAppNotification_ShouldSaveNotification() {
    // Arrange
    Notification savedNotification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .title("Test Title")
            .body("Test Body")
            .type(NotificationType.LOAN)
            .isRead(false)
            .build();

    when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

    // Act
    notificationService.sendInAppNotification(
        userId, "Test Title", "Test Body", NotificationType.LOAN, null, null);

    // Assert
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("SendInAppNotification should handle exception gracefully")
  void sendInAppNotification_ShouldHandleException() {
    // Arrange
    when(notificationRepository.save(any(Notification.class)))
        .thenThrow(new RuntimeException("Database error"));

    // Act - Should not throw exception
    assertDoesNotThrow(
        () ->
            notificationService.sendInAppNotification(
                userId, "Test Title", "Test Body", NotificationType.LOAN, null, null));

    // Assert
    verify(notificationRepository).save(any(Notification.class));
  }

  @Test
  @DisplayName("NotifyLoanStatusChange should send all notification types")
  void notifyLoanStatusChange_ShouldSendAllNotifications() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    notificationService.notifyLoanStatusChange(userId, LoanStatus.APPROVED);

    // Assert
    verify(userRepository).findById(userId);
    verify(notificationRepository).save(any(Notification.class));
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("NotifyLoanStatusChange should handle user not found")
  void notifyLoanStatusChange_ShouldHandleUserNotFound() {
    // Arrange
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act
    notificationService.notifyLoanStatusChange(userId, LoanStatus.APPROVED);

    // Assert
    verify(userRepository).findById(userId);
    verify(notificationRepository, never()).save(any());
    verify(javaMailSender, never()).send(any());
  }

  @Test
  @DisplayName("NotifyForgotPassword should send email")
  void notifyForgotPassword_ShouldSendEmail() {
    // Arrange
    doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

    // Act
    notificationService.notifyForgotPassword("user@example.com", "reset-token-123");

    // Assert
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("NotifyPasswordResetSuccess should send email")
  void notifyPasswordResetSuccess_ShouldSendEmail() {
    // Arrange
    doNothing().when(javaMailSender).send(any(SimpleMailMessage.class));

    // Act
    notificationService.notifyPasswordResetSuccess("user@example.com");

    // Assert
    verify(javaMailSender).send(any(SimpleMailMessage.class));
  }

  @Test
  @DisplayName("GetNotifications should return user notifications")
  void getNotifications_ShouldReturnUserNotifications() {
    // Arrange
    Notification notification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .title("Test")
            .body("Test Body")
            .type(NotificationType.LOAN)
            .isRead(false)
            .build();

    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
        .thenReturn(List.of(notification));

    // Act
    List<NotificationResponse> result = notificationService.getNotifications(userId);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test", result.get(0).getTitle());
    verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(userId);
  }

  @Test
  @DisplayName("MarkAsRead should mark notification as read")
  void markAsRead_ShouldMarkNotificationAsRead() {
    // Arrange
    Notification notification =
        Notification.builder()
            .id(notificationId)
            .userId(userId)
            .title("Test")
            .isRead(false)
            .build();

    when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
    when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

    // Act
    notificationService.markAsRead(notificationId);

    // Assert
    assertTrue(notification.getIsRead());
    verify(notificationRepository).findById(notificationId);
    verify(notificationRepository).save(notification);
  }

  @Test
  @DisplayName("MarkAsRead should handle notification not found")
  void markAsRead_ShouldHandleNotificationNotFound() {
    // Arrange
    when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

    // Act - Should not throw exception
    assertDoesNotThrow(() -> notificationService.markAsRead(notificationId));

    // Assert
    verify(notificationRepository).findById(notificationId);
    verify(notificationRepository, never()).save(any());
  }
}
