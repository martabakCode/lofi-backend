package com.lofi.lofiapps.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.enums.DocumentType;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.service.impl.DocumentServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentControllerTest {

  private MockMvc mockMvc;

  @Mock private DocumentServiceImpl documentService;

  @Mock private JwtUtils jwtUtils;

  @InjectMocks private DocumentController documentController;

  private ObjectMapper objectMapper;
  private UUID userId;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(documentController).build();
    objectMapper = new ObjectMapper();
    userId = UUID.randomUUID();
    setupSecurityContext();
  }

  private void setupSecurityContext() {
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    // Explicitly define the list type to match GrantedAuthority
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    doReturn(authorities).when(authentication).getAuthorities();
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @DisplayName("Presign upload should return upload URL")
  void presignUpload_ShouldReturnUploadUrl() throws Exception {
    PresignUploadRequest request = new PresignUploadRequest();
    request.setFileName("test.pdf");
    request.setDocumentType(DocumentType.KTP);
    request.setContentType("application/pdf");

    PresignUploadResponse response =
        PresignUploadResponse.builder().uploadUrl("https://s3.example.com/upload").build();

    when(jwtUtils.getUserIdFromJwtToken("valid-token")).thenReturn(userId.toString());
    when(documentService.presignUpload(any(PresignUploadRequest.class), eq(userId)))
        .thenReturn(response);

    mockMvc
        .perform(
            post("/documents/presign-upload")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.uploadUrl").value("https://s3.example.com/upload"));

    verify(documentService, times(1)).presignUpload(any(PresignUploadRequest.class), eq(userId));
  }

  @Test
  @DisplayName("Presign download should return download URL")
  void presignDownload_ShouldReturnDownloadUrl() throws Exception {
    UUID documentId = UUID.randomUUID();
    DownloadDocumentResponse response =
        DownloadDocumentResponse.builder().downloadUrl("https://s3.example.com/download").build();

    when(jwtUtils.getUserIdFromJwtToken("valid-token")).thenReturn(userId.toString());
    when(documentService.presignDownload(eq(documentId), eq(userId), anyBoolean()))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/documents/{id}/download", documentId)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.downloadUrl").value("https://s3.example.com/download"));

    verify(documentService, times(1)).presignDownload(eq(documentId), eq(userId), anyBoolean());
  }

  @Test
  @DisplayName("Presign download as admin should pass isAdmin true")
  void presignDownload_AsAdmin_ShouldPassIsAdminTrue() throws Exception {
    UUID documentId = UUID.randomUUID();
    DownloadDocumentResponse response =
        DownloadDocumentResponse.builder().downloadUrl("https://s3.example.com/download").build();

    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);

    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
    doReturn(authorities).when(authentication).getAuthorities();
    SecurityContextHolder.setContext(securityContext);

    when(jwtUtils.getUserIdFromJwtToken("valid-token")).thenReturn(userId.toString());
    when(documentService.presignDownload(eq(documentId), eq(userId), eq(true)))
        .thenReturn(response);

    mockMvc
        .perform(
            get("/documents/{id}/download", documentId)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    verify(documentService, times(1)).presignDownload(eq(documentId), eq(userId), eq(true));
  }
}
