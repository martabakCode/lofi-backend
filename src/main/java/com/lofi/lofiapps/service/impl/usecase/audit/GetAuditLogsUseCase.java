package com.lofi.lofiapps.service.impl.usecase.audit;

import com.lofi.lofiapps.dto.response.AuditLogResponse;
import com.lofi.lofiapps.dto.response.PagedResponse;
import com.lofi.lofiapps.entity.AuditLog;
import com.lofi.lofiapps.repository.AuditLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAuditLogsUseCase {

  private final AuditLogRepository auditLogRepository;

  public PagedResponse<AuditLogResponse> execute(Pageable pageable) {
    Page<AuditLog> page = auditLogRepository.findAll(pageable);

    List<AuditLogResponse> items =
        page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList());

    return PagedResponse.of(
        items, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  private AuditLogResponse mapToResponse(AuditLog log) {
    return AuditLogResponse.builder()
        .id(log.getId())
        .userId(log.getUserId())
        .action(log.getAction())
        .resourceType(log.getResourceType())
        .resourceId(log.getResourceId())
        .details(log.getDetails())
        .createdAt(log.getCreatedAt())
        .build();
  }
}
