package com.lofi.lofiapps.controller;

import com.lofi.lofiapps.dto.response.*;
import com.lofi.lofiapps.dto.response.AuditLogResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.service.impl.audit.GetAuditLogsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit Logs Management")
public class AuditController {
  private final GetAuditLogsUseCase getAuditLogsUseCase;

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
  @Operation(summary = "Get audit logs")
  public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
      @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
    return ResponseEntity.ok(ApiResponse.success(getAuditLogsUseCase.execute(pageable)));
  }
}
