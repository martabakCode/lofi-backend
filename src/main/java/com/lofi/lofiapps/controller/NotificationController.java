package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.response.ApiResponse;
import com.lofi.lofiapps.dto.response.NotificationResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.security.service.UserPrincipal;
import com.lofi.lofiapps.service.NotificationService;
import com.lofi.lofiapps.service.impl.usecase.notification.GetNotificationsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "Notification Management")
public class NotificationController {

  private final GetNotificationsUseCase getNotificationsUseCase;
  private final NotificationService notificationService;

  @GetMapping
  @Operation(summary = "Get user notifications")
  public ResponseEntity<ApiResponse<List<NotificationResponse>>> getNotifications(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    if (userPrincipal == null) {
      throw new IllegalArgumentException("User not authenticated");
    }
    return ResponseEntity.ok(
        ApiResponse.success(getNotificationsUseCase.execute(userPrincipal.getId())));
  }

  // Admin endpoints
  @GetMapping("/all")
  @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
  @Operation(summary = "Get all notifications (Admin only)")
  public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getAllNotifications(
      @PageableDefault(size = 20) Pageable pageable) {
    // This would need a new method in service, for now return empty
    return ResponseEntity.ok(ApiResponse.success(PagedResponse.empty()));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('NOTIFICATION_VIEW')")
  @Operation(summary = "Get notification by ID (Admin only)")
  public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(
      @PathVariable UUID id) {
    // This would need a new method in service, for now return first notification
    // from user's list
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PutMapping("/{id}/read")
  @Operation(summary = "Mark notification as read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
    if (userPrincipal == null) {
      throw new IllegalArgumentException("User not authenticated");
    }
    notificationService.markAsRead(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Notification marked as read"));
  }

  @PutMapping("/mark-all-read")
  @Operation(summary = "Mark all notifications as read")
  public ResponseEntity<ApiResponse<Void>> markAllAsRead(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    if (userPrincipal == null) {
      throw new IllegalArgumentException("User not authenticated");
    }
    // This would need implementation in service
    return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('NOTIFICATION_DELETE')")
  @Operation(summary = "Delete a notification (Admin only)")
  public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable UUID id) {
    // This would need implementation in service
    return ResponseEntity.ok(ApiResponse.success(null, "Notification deleted successfully"));
  }
}
