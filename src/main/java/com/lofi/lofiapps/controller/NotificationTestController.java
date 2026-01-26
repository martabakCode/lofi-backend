package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class NotificationTestController {

  private final NotificationService notificationService;

  @PostMapping("/notification")
  public ResponseEntity<String> sendTestNotification(@RequestBody TestNotificationRequest request) {
    notificationService.sendPushNotification(
        request.getToken(), request.getTitle(), request.getBody());
    return ResponseEntity.ok("Notification sent (check logs for success/failure)");
  }

  @lombok.Data
  public static class TestNotificationRequest {
    private String token;
    private String title;
    private String body;
  }
}
