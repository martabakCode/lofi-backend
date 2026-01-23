package com.lofi.lofiapps.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PresignUploadResponse {
  private UUID documentId;
  private String uploadUrl;
  private String objectKey;
}
