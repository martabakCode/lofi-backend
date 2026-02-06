package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetGooglePinRequest {
  @NotBlank(message = "PIN is required")
  @Size(min = 4, max = 6, message = "PIN must be between 4 and 6 digits")
  private String pin;
}
