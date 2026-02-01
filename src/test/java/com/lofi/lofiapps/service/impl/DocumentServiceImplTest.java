package com.lofi.lofiapps.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.service.impl.usecase.document.GetPresignedDownloadUrlUseCase;
import com.lofi.lofiapps.service.impl.usecase.document.PresignUploadUseCase;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

  @Mock private PresignUploadUseCase presignUploadUseCase;
  @Mock private GetPresignedDownloadUrlUseCase getPresignedDownloadUrlUseCase;

  @InjectMocks private DocumentServiceImpl documentService;

  private UUID userId;
  private UUID documentId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    documentId = UUID.randomUUID();
  }

  @Test
  @DisplayName("PresignUpload should delegate to PresignUploadUseCase")
  void presignUpload_ShouldDelegateToUseCase() {
    // Arrange
    PresignUploadRequest request =
        PresignUploadRequest.builder().fileName("test.pdf").contentType("application/pdf").build();

    PresignUploadResponse expectedResponse =
        PresignUploadResponse.builder()
            .presignedUrl("https://presigned-url.example.com")
            .documentId(documentId)
            .build();

    when(presignUploadUseCase.execute(any(PresignUploadRequest.class), any(UUID.class)))
        .thenReturn(expectedResponse);

    // Act
    PresignUploadResponse result = documentService.presignUpload(request, userId);

    // Assert
    assertNotNull(result);
    assertEquals(documentId, result.getDocumentId());
    assertEquals("https://presigned-url.example.com", result.getPresignedUrl());
    verify(presignUploadUseCase).execute(request, userId);
  }

  @Test
  @DisplayName("PresignDownload should delegate to GetPresignedDownloadUrlUseCase")
  void presignDownload_ShouldDelegateToUseCase() {
    // Arrange
    DownloadDocumentResponse expectedResponse =
        DownloadDocumentResponse.builder()
            .presignedUrl("https://download-url.example.com")
            .fileName("test.pdf")
            .contentType("application/pdf")
            .build();

    when(getPresignedDownloadUrlUseCase.execute(any(UUID.class), any(UUID.class), anyBoolean()))
        .thenReturn(expectedResponse);

    // Act
    DownloadDocumentResponse result = documentService.presignDownload(documentId, userId, false);

    // Assert
    assertNotNull(result);
    assertEquals("https://download-url.example.com", result.getPresignedUrl());
    assertEquals("test.pdf", result.getFileName());
    verify(getPresignedDownloadUrlUseCase).execute(documentId, userId, false);
  }

  @Test
  @DisplayName("PresignDownload should work for admin user")
  void presignDownload_ShouldWorkForAdmin() {
    // Arrange
    DownloadDocumentResponse expectedResponse =
        DownloadDocumentResponse.builder()
            .presignedUrl("https://admin-download-url.example.com")
            .fileName("admin-test.pdf")
            .contentType("application/pdf")
            .build();

    when(getPresignedDownloadUrlUseCase.execute(any(UUID.class), any(UUID.class), eq(true)))
        .thenReturn(expectedResponse);

    // Act
    DownloadDocumentResponse result = documentService.presignDownload(documentId, userId, true);

    // Assert
    assertNotNull(result);
    assertEquals("https://admin-download-url.example.com", result.getPresignedUrl());
    verify(getPresignedDownloadUrlUseCase).execute(documentId, userId, true);
  }
}
