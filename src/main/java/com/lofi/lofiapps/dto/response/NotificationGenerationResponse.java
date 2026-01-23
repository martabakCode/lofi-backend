package com.lofi.lofiapps.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationGenerationResponse {
  private String title;
  private String message;
}
