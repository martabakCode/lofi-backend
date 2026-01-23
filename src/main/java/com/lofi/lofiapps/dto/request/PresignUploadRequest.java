package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Data;

@Data
public class PresignUploadRequest {
  private UUID loanId;

  @NotBlank(message = "File name is required")
  private String fileName;

  @NotNull(message = "Document type is required")
  private DocumentType documentType;

  private String contentType;
}
