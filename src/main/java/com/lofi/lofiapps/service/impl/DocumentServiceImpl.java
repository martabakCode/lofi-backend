package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.service.DocumentService;
import com.lofi.lofiapps.service.impl.usecase.document.GetPresignedDownloadUrlUseCase;
import com.lofi.lofiapps.service.impl.usecase.document.PresignUploadUseCase;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

  private final PresignUploadUseCase presignUploadUseCase;
  private final GetPresignedDownloadUrlUseCase getPresignedDownloadUrlUseCase;

  @Override
  public PresignUploadResponse presignUpload(PresignUploadRequest request, UUID userId) {
    return presignUploadUseCase.execute(request, userId);
  }

  @Override
  public DownloadDocumentResponse presignDownload(UUID id, UUID userId, boolean isAdmin) {
    return getPresignedDownloadUrlUseCase.execute(id, userId, isAdmin);
  }
}
