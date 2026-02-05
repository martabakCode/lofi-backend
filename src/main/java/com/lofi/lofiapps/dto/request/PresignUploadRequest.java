package com.lofi.lofiapps.dto.request;

import com.lofi.lofiapps.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignUploadRequest {
  private UUID loanId;

  @NotBlank(message = "File name is required")
  private String fileName;

  @NotNull(message = "Document type is required")
  private DocumentType documentType;

  @NotBlank(message = "Content type is required")
  private String contentType;

  // File size in bytes (optional, for validation)
  private Long fileSize;
}
