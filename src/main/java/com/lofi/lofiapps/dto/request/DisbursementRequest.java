package com.lofi.lofiapps.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DisbursementRequest {
  @NotBlank(message = "Reference number is required")
  private String referenceNumber;

  private String bankName;
  private String accountNumber;
}
