package com.lofi.lofiapps.service;

public interface NotificationService {
  void sendEmail(String to, String subject, String body);

  void sendPushNotification(String token, String title, String body);

  void sendInAppNotification(
      java.util.UUID userId,
      String title,
      String body,
      com.lofi.lofiapps.enums.NotificationType type,
      java.util.UUID referenceId,
      String link);

  void notifyLoanStatusChange(java.util.UUID userId, com.lofi.lofiapps.enums.LoanStatus newStatus);

  void notifyForgotPassword(String email, String token);

  void notifyPasswordResetSuccess(String email);

  java.util.List<com.lofi.lofiapps.dto.response.NotificationResponse> getNotifications(
      java.util.UUID userId);

  void notifyLoanDisbursement(com.lofi.lofiapps.entity.Loan loan);

  void notifyPinReset(String email, String newPin);

  void notifyPinRequired(java.util.UUID userId);

  void markAsRead(java.util.UUID id);
}
