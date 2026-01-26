package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import java.util.UUID;

public interface DocumentService {
  PresignUploadResponse presignUpload(PresignUploadRequest request, UUID userId);

  DownloadDocumentResponse presignDownload(UUID id, UUID userId, boolean isAdmin);
}
