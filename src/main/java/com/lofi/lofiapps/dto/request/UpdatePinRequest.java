package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePinRequest {
  @NotBlank(message = "Old PIN is required")
  private String oldPin;

  @NotBlank(message = "New PIN is required")
  @Pattern(regexp = "^\\d{6}$", message = "PIN must be 6 digits")
  private String newPin;
}
