package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.dto.response.AuditLogResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.service.AuditService;
import com.lofi.lofiapps.service.impl.usecase.audit.GetAuditLogsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

  private final GetAuditLogsUseCase getAuditLogsUseCase;

  @Override
  public PagedResponse<AuditLogResponse> getAuditLogs(Pageable pageable) {
    return getAuditLogsUseCase.execute(pageable);
  }
}
