package com.lofi.lofiapps.service.impl.audit;

import com.lofi.lofiapps.model.entity.JpaAuditLog;
import com.lofi.lofiapps.repository.JpaAuditLogRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogActivityUseCase {
  private final JpaAuditLogRepository auditLogRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void execute(
      UUID userId, String action, String resourceType, String resourceId, String details) {
    JpaAuditLog log =
        JpaAuditLog.builder()
            .userId(userId)
            .action(action)
            .resourceType(resourceType)
            .resourceId(resourceId)
            .details(details)
            .build();
    auditLogRepository.save(log);
  }
}
