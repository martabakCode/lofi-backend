package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectLoanRequest {
  @NotBlank(message = "Rejection reason is required")
  private String reason;
}
