package com.lofi.lofiapps.dto.response;

import com.lofi.lofiapps.enums.DocumentType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponse {
  private UUID id;
  private String fileName;
  private DocumentType documentType;
  private LocalDateTime uploadedAt;
}
