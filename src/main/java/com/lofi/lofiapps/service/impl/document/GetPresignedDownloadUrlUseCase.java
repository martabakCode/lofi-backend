package com.lofi.lofiapps.service.impl.document;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.model.entity.JpaDocument;
import com.lofi.lofiapps.repository.JpaDocumentRepository;
import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetPresignedDownloadUrlUseCase {
  private final JpaDocumentRepository documentRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  public DownloadDocumentResponse execute(UUID documentId, UUID userId, boolean isAdmin) {
    JpaDocument document =
        documentRepository
            .findById(documentId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Document", "id", documentId.toString()));

    // Security check: Only owner or admin can download
    if (!isAdmin && !document.getUploadedBy().equals(userId)) {
      throw new AccessDeniedException("You do not have permission to download this document");
    }

    URL downloadUrl =
        storageService.generatePresignedDownloadUrl(bucketName, document.getObjectKey(), 15);

    return DownloadDocumentResponse.builder()
        .downloadUrl(downloadUrl.toString())
        .fileName(document.getFileName())
        .build();
  }
}
