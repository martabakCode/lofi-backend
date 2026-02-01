package com.lofi.lofiapps.service.impl.usecase.notification;

import com.lofi.lofiapps.dto.response.NotificationResponse;
import com.lofi.lofiapps.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetNotificationsUseCase {

  private final NotificationService notificationService;

  public List<NotificationResponse> execute(UUID userId) {
    return notificationService.getNotifications(userId);
  }
}
