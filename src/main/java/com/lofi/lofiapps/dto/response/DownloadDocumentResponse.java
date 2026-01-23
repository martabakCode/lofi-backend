package com.lofi.lofiapps.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DownloadDocumentResponse {
  private String downloadUrl;
  private String fileName;
}
