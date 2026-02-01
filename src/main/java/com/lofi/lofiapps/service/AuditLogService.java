package com.lofi.lofiapps.service;

import java.util.UUID;

/**
 * Service interface for comprehensive audit logging. Tracks all security-relevant operations for
 * compliance.
 */
public interface AuditLogService {

  /**
   * Logs a security event.
   *
   * @param action the action performed (e.g., "LOAN_APPROVE", "USER_LOGIN")
   * @param entityType the type of entity affected (e.g., "Loan", "User")
   * @param entityId the ID of the entity affected
   * @param userId the ID of the user performing the action
   * @param oldValue the previous value (for updates)
   * @param newValue the new value
   * @param ipAddress the IP address of the request
   * @param userAgent the user agent of the request
   * @param description additional description
   */
  void log(
      String action,
      String entityType,
      UUID entityId,
      UUID userId,
      String oldValue,
      String newValue,
      String ipAddress,
      String userAgent,
      String description);

  /**
   * Logs a login attempt.
   *
   * @param email the email attempting login
   * @param success whether the login was successful
   * @param ipAddress the IP address
   * @param userAgent the user agent
   * @param failureReason reason for failure if unsuccessful
   */
  void logLogin(
      String email, boolean success, String ipAddress, String userAgent, String failureReason);

  /**
   * Logs access to sensitive data.
   *
   * @param userId the user accessing data
   * @param resourceType the type of resource accessed
   * @param resourceId the ID of the resource
   * @param ipAddress the IP address
   * @param accessType type of access (READ, WRITE, DELETE)
   */
  void logDataAccess(
      UUID userId, String resourceType, UUID resourceId, String ipAddress, String accessType);

  /**
   * Logs permission/authorization events.
   *
   * @param userId the user involved
   * @param action the action attempted
   * @param granted whether permission was granted
   * @param reason reason for denial if not granted
   */
  void logAuthorization(UUID userId, String action, boolean granted, String reason);

  /**
   * Logs system configuration changes.
   *
   * @param userId the user making changes
   * @param configType the type of configuration
   * @param oldConfig old configuration value
   * @param newConfig new configuration value
   */
  void logConfigChange(UUID userId, String configType, String oldConfig, String newConfig);
}
