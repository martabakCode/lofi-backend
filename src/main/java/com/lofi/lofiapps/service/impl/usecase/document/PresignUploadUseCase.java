package com.lofi.lofiapps.service.impl.usecase.document;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.entity.Document;
import com.lofi.lofiapps.repository.DocumentRepository;
import com.lofi.lofiapps.service.StorageService;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Use case for generating presigned upload URLs with file type validation. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PresignUploadUseCase {

  private final DocumentRepository documentRepository;
  private final StorageService storageService;

  @Value("${app.storage.bucket-name:lofi-bucket}")
  private String bucketName;

  // Allowed MIME types for document uploads
  private static final Set<String> ALLOWED_CONTENT_TYPES =
      new HashSet<>(
          Arrays.asList(
              "image/jpeg",
              "image/jpg",
              "image/png",
              "image/gif",
              "application/pdf",
              "application/msword",
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document"));

  // Maximum file size: 10MB
  private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

  @Transactional
  public PresignUploadResponse execute(PresignUploadRequest request, UUID userId) {
    // Validate file type
    validateContentType(request.getContentType());

    // Validate file size if provided
    if (request.getFileSize() != null && request.getFileSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
    }

    // Validate file name
    validateFileName(request.getFileName());

    String objectKey =
        String.format(
            "documents/%s/%s_%s",
            request.getLoanId(), UUID.randomUUID(), sanitizeFileName(request.getFileName()));

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
            .contentType(request.getContentType())
            .fileSize(request.getFileSize())
            .build();

    Document savedDoc = documentRepository.save(document);

    log.info("Generated presigned upload URL for document {} by user {}", savedDoc.getId(), userId);

    return PresignUploadResponse.builder()
        .documentId(savedDoc.getId())
        .uploadUrl(uploadUrl.toString())
        .objectKey(objectKey)
        .build();
  }

  private void validateContentType(String contentType) {
    if (contentType == null || contentType.isBlank()) {
      throw new IllegalArgumentException("Content type is required");
    }

    // Normalize content type
    String normalizedType = contentType.toLowerCase().trim();

    if (!ALLOWED_CONTENT_TYPES.contains(normalizedType)) {
      throw new IllegalArgumentException(
          "File type not allowed. Allowed types: JPEG, PNG, GIF, PDF, DOC, DOCX");
    }
  }

  private void validateFileName(String fileName) {
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("File name is required");
    }

    // Check for path traversal attempts
    if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
      throw new IllegalArgumentException("Invalid file name");
    }

    // Check file extension
    String lowerFileName = fileName.toLowerCase();
    boolean hasValidExtension =
        lowerFileName.endsWith(".jpg")
            || lowerFileName.endsWith(".jpeg")
            || lowerFileName.endsWith(".png")
            || lowerFileName.endsWith(".gif")
            || lowerFileName.endsWith(".pdf")
            || lowerFileName.endsWith(".doc")
            || lowerFileName.endsWith(".docx");

    if (!hasValidExtension) {
      throw new IllegalArgumentException(
          "File must have a valid extension: .jpg, .jpeg, .png, .gif, .pdf, .doc, .docx");
    }
  }

  private String sanitizeFileName(String fileName) {
    // Remove any path components and special characters
    String sanitized = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    // Limit length
    if (sanitized.length() > 100) {
      sanitized = sanitized.substring(0, 100);
    }
    return sanitized;
  }
}
