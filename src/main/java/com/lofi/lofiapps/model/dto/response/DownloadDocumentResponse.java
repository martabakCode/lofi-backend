package com.lofi.lofiapps.model.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadDocumentResponse {
  private String downloadUrl;
  private String fileName;
}
