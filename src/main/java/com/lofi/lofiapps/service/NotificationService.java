package com.lofi.lofiapps.service;

public interface NotificationService {
  void sendEmail(String to, String subject, String body);

  void sendPushNotification(String token, String title, String message);

  void sendInAppNotification(
      java.util.UUID userId, String title, String message, String type, String link);

  void notifyLoanStatusChange(
      java.util.UUID userId, com.lofi.lofiapps.model.enums.LoanStatus newStatus);

  void notifyForgotPassword(String email, String token);

  void notifyPasswordResetSuccess(String email);
}
