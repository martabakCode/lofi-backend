package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleLoginRequest {
  @NotBlank private String idToken;

  private Double latitude;
  private Double longitude;
}
