package com.lofi.lofiapps.service.impl.document;

import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.model.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.model.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.model.entity.JpaDocument;
import com.lofi.lofiapps.repository.JpaDocumentRepository;
import com.lofi.lofiapps.repository.LoanRepository;
import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PresignUploadUseCase {
  private final JpaDocumentRepository documentRepository;
  private final LoanRepository loanRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  @Transactional
  public PresignUploadResponse execute(PresignUploadRequest request, UUID userId) {
    // 1. Verify loan exists (if provided)
    if (request.getLoanId() != null) {
      loanRepository
          .findById(request.getLoanId())
          .orElseThrow(
              () -> new ResourceNotFoundException("Loan", "id", request.getLoanId().toString()));
    }

    // 2. Create object key
    String objectKey;
    if (request.getDocumentType() == com.lofi.lofiapps.model.enums.DocumentType.PROFILE_PICTURE) {
      objectKey =
          String.format(
              "profiles/%s/%s_%s",
              userId, UUID.randomUUID().toString().substring(0, 8), request.getFileName());
    } else {
      objectKey =
          String.format(
              "loans/%s/%s_%s_%s",
              request.getLoanId(),
              request.getDocumentType(),
              UUID.randomUUID().toString().substring(0, 8),
              request.getFileName());
    }

    // 3. Save document record (initially could be status PENDING, but for
    // simplicity we save it)
    JpaDocument document =
        JpaDocument.builder()
            .loanId(request.getLoanId())
            .fileName(request.getFileName())
            .objectKey(objectKey)
            .documentType(request.getDocumentType())
            .uploadedBy(userId)
            .build();

    JpaDocument saved = documentRepository.save(document);

    // 4. Generate presigned URL (valid for 15 minutes)
    URL uploadUrl =
        storageService.generatePresignedUploadUrl(
            bucketName, objectKey, request.getContentType(), 15);

    return PresignUploadResponse.builder()
        .documentId(saved.getId())
        .uploadUrl(uploadUrl.toString())
        .objectKey(objectKey)
        .build();
  }
}
