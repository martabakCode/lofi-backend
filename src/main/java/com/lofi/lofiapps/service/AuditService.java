package com.lofi.lofiapps.service;

import com.lofi.lofiapps.dto.response.AuditLogResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface AuditService {
  PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable);
}
