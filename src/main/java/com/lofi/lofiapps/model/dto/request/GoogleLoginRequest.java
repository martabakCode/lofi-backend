package com.lofi.lofiapps.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
  @NotBlank private String idToken;

  private Double latitude;
  private Double longitude;
}
