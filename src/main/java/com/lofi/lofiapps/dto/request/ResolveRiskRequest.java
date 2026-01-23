package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResolveRiskRequest {
  @NotBlank(message = "Resolution comments are required")
  private String comments;
}
