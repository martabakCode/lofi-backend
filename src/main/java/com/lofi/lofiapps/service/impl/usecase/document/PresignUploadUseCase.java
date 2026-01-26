package com.lofi.lofiapps.service.impl.usecase.document;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.entity.Document;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PresignUploadUseCase {

  private final DocumentRepository documentRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  @Transactional
  public PresignUploadResponse execute(PresignUploadRequest request, UUID userId) {
    String objectKey =
        String.format(
            "documents/%s/%s_%s", request.getLoanId(), UUID.randomUUID(), request.getFileName());

    URL uploadUrl =
        storageService.generatePresignedUploadUrl(
            bucketName, objectKey, request.getContentType(), 30 // 30 minutes expiration
            );

    Document document =
        Document.builder()
            .loanId(request.getLoanId())
            .fileName(request.getFileName())
            .objectKey(objectKey)
            .documentType(request.getDocumentType())
            .uploadedBy(userId)
            .build();

    Document savedDoc = documentRepository.save(document);

    return PresignUploadResponse.builder()
        .documentId(savedDoc.getId())
        .uploadUrl(uploadUrl.toString())
        .objectKey(objectKey)
        .build();
  }
}
