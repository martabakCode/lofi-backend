package com.lofi.lofiapps.service.impl.audit;

import com.lofi.lofiapps.entity.AuditLog;
import com.lofi.lofiapps.repository.AuditLogRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogActivityUseCase {
  private final AuditLogRepository auditLogRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void execute(
      UUID userId, String action, String resourceType, String resourceId, String details) {
    AuditLog log =
        AuditLog.builder()
            .userId(userId)
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .details(details)
            .build();
    auditLogRepository.save(log);
  }
}
