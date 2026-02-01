package com.lofi.lofiapps.service.impl;

import com.lofi.lofiapps.entity.AuditLog;
import com.lofi.lofiapps.repository.AuditLogRepository;
import com.lofi.lofiapps.service.AuditLogService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuditLogService. Provides comprehensive audit logging for compliance and
 * security monitoring.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void log(
      String action,
      String entityType,
      UUID entityId,
      UUID userId,
      String oldValue,
      String newValue,
      String ipAddress,
      String userAgent,
      String description) {
    try {
      AuditLog auditLog =
          AuditLog.builder()
              .action(action)
              .entityType(entityType)
              .entityId(entityId)
              .userId(userId)
              .oldValue(oldValue)
              .newValue(newValue)
              .ipAddress(ipAddress)
              .userAgent(userAgent)
              .description(description)
              .createdAt(LocalDateTime.now())
              .build();

      auditLogRepository.save(auditLog);
      log.debug("Audit log created: {} on {}:{}", action, entityType, entityId);
    } catch (Exception e) {
      // Never fail the main operation due to audit logging failure
      log.error("Failed to create audit log: {}", e.getMessage(), e);
    }
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logLogin(
      String email, boolean success, String ipAddress, String userAgent, String failureReason) {
    try {
      String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILURE";
      String description =
          success ? "User logged in successfully" : "Login failed: " + failureReason;

      AuditLog auditLog =
          AuditLog.builder()
              .action(action)
              .entityType("User")
              .entityId(null) // Will be updated if user found
              .userId(null)
              .ipAddress(ipAddress)
              .userAgent(userAgent)
              .description(description + " | Email: " + maskEmail(email))
              .createdAt(LocalDateTime.now())
              .build();

      auditLogRepository.save(auditLog);
      log.info("Login audit logged for {}: success={}", maskEmail(email), success);
    } catch (Exception e) {
      log.error("Failed to create login audit log: {}", e.getMessage(), e);
    }
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logDataAccess(
      UUID userId, String resourceType, UUID resourceId, String ipAddress, String accessType) {
    try {
      AuditLog auditLog =
          AuditLog.builder()
              .action("DATA_ACCESS_" + accessType)
              .entityType(resourceType)
              .entityId(resourceId)
              .userId(userId)
              .ipAddress(ipAddress)
              .description("Data accessed: " + resourceType + " by user " + userId)
              .createdAt(LocalDateTime.now())
              .build();

      auditLogRepository.save(auditLog);
      log.debug("Data access logged: {} on {}:{}", accessType, resourceType, resourceId);
    } catch (Exception e) {
      log.error("Failed to create data access audit log: {}", e.getMessage(), e);
    }
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAuthorization(UUID userId, String action, boolean granted, String reason) {
    try {
      String actionType = granted ? "AUTHZ_GRANT" : "AUTHZ_DENY";
      String description =
          granted
              ? "Permission granted for: " + action
              : "Permission denied for: " + action + " | Reason: " + reason;

      AuditLog auditLog =
          AuditLog.builder()
              .action(actionType)
              .entityType("Authorization")
              .entityId(null)
              .userId(userId)
              .description(description)
              .createdAt(LocalDateTime.now())
              .build();

      auditLogRepository.save(auditLog);
      log.info(
          "Authorization audit logged for user {}: action={}, granted={}", userId, action, granted);
    } catch (Exception e) {
      log.error("Failed to create authorization audit log: {}", e.getMessage(), e);
    }
  }

  @Override
  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logConfigChange(UUID userId, String configType, String oldConfig, String newConfig) {
    try {
      AuditLog auditLog =
          AuditLog.builder()
              .action("CONFIG_CHANGE")
              .entityType("Configuration")
              .entityId(null)
              .userId(userId)
              .oldValue(oldConfig)
              .newValue(newConfig)
              .description("Configuration changed: " + configType)
              .createdAt(LocalDateTime.now())
              .build();

      auditLogRepository.save(auditLog);
      log.warn("Configuration change logged by user {}: {}", userId, configType);
    } catch (Exception e) {
      log.error("Failed to create config change audit log: {}", e.getMessage(), e);
    }
  }

  /** Masks email for privacy in logs. */
  private String maskEmail(String email) {
    if (email == null || email.isEmpty() || !email.contains("@")) {
      return "***";
    }
    String[] parts = email.split("@");
    String local = parts[0];
    String domain = parts[1];

    if (local.length() <= 2) {
      return "***@" + domain;
    }

    return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
  }
}
