package com.lofi.lofiapps.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationGenerationResponse {
  private String title;
  private String message;
}
