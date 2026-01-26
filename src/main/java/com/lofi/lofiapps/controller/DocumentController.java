package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.request.PresignUploadRequest;
import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.dto.response.DownloadDocumentResponse;
import com.lofi.lofiapps.dto.response.PresignUploadResponse;
import com.lofi.lofiapps.security.jwt.JwtUtils;
import com.lofi.lofiapps.service.impl.document.GetPresignedDownloadUrlUseCase;
import com.lofi.lofiapps.service.impl.document.PresignUploadUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Document", description = "Document Management")
public class DocumentController {
  private final PresignUploadUseCase presignUploadUseCase;
  private final GetPresignedDownloadUrlUseCase getPresignedDownloadUrlUseCase;
  private final JwtUtils jwtUtils;

  @PostMapping("/presign-upload")
  @Operation(summary = "Presign upload document")
  public ResponseEntity<ApiResponse<PresignUploadResponse>> presignUpload(
      @Valid @RequestBody PresignUploadRequest request, HttpServletRequest httpRequest) {

    UUID userId = getCurrentUserId(httpRequest);
    return ResponseEntity.ok(ApiResponse.success(presignUploadUseCase.execute(request, userId)));
  }

  @GetMapping("/{id}/download")
  @Operation(summary = "Presign download document")
  public ResponseEntity<ApiResponse<DownloadDocumentResponse>> presignDownload(
      @PathVariable UUID id, HttpServletRequest httpRequest) {

    UUID userId = getCurrentUserId(httpRequest);
    boolean isAdmin =
        SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
            .anyMatch(
                a ->
                    a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

    return ResponseEntity.ok(
        ApiResponse.success(getPresignedDownloadUrlUseCase.execute(id, userId, isAdmin)));
  }

  private UUID getCurrentUserId(HttpServletRequest request) {
    String token = parseJwt(request);
    return UUID.fromString(jwtUtils.getUserIdFromJwtToken(token));
  }

  private String parseJwt(HttpServletRequest request) {
    String headerAuth = request.getHeader("Authorization");
    if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
      return headerAuth.substring(7);
    }
    return null;
  }
}
