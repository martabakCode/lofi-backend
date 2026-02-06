package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationTestControllerTest {

  private MockMvc mockMvc;

  @Mock private NotificationService notificationService;

  @InjectMocks private NotificationTestController notificationTestController;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(notificationTestController).build();
    objectMapper = new ObjectMapper();
  }

  @Test
  @DisplayName("Send test notification should return success message")
  void sendTestNotification_ShouldReturnSuccess() throws Exception {
    NotificationTestController.TestNotificationRequest request =
        new NotificationTestController.TestNotificationRequest();
    request.setToken("test-token");
    request.setTitle("Test Title");
    request.setBody("Test Body");

    doNothing()
        .when(notificationService)
        .sendPushNotification(anyString(), anyString(), anyString());

    mockMvc
        .perform(
            post("/test/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("Notification sent (check logs for success/failure)"));

    verify(notificationService, times(1))
        .sendPushNotification("test-token", "Test Title", "Test Body");
  }
}
