package com.lofi.lofiapps.service.impl.usecase.document;

import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.entity.Document;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPresignedDownloadUrlUseCase {

  private final DocumentRepository documentRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  public DownloadDocumentResponse execute(UUID id, UUID userId, boolean isAdmin) {
    Document document =
        documentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id.toString()));

    // Check ownership if not admin
    if (!isAdmin && !document.getUploadedBy().equals(userId)) {
      throw new RuntimeException("Unauthorized: You do not own this document");
    }

    URL downloadUrl =
        storageService.generatePresignedDownloadUrl(
            bucketName, document.getObjectKey(), 60 // 60 minutes expiration
            );

    return DownloadDocumentResponse.builder()
        .downloadUrl(downloadUrl.toString())
        .fileName(document.getFileName())
        .build();
  }
}
