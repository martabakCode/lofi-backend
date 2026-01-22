package com.lofi.lofiapps.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RefreshTokenRequest {
  @NotBlank(message = "Refresh token is required")
  private String refreshToken;
}
