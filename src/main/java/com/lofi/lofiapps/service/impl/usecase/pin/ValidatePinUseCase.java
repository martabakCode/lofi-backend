package com.lofi.lofiapps.service.impl.usecase.pin;

import com.lofi.lofiapps.entity.AuditLog;
import com.lofi.lofiapps.entity.User;
import com.lofi.lofiapps.exception.PinValidationException;
import com.lofi.lofiapps.exception.ResourceNotFoundException;
import com.lofi.lofiapps.repository.AuditLogRepository;
import com.lofi.lofiapps.repository.UserRepository;
import com.lofi.lofiapps.service.RateLimitService;
import com.lofi.lofiapps.service.impl.validator.PinValidator;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidatePinUseCase {

  private final UserRepository userRepository;
  private final PinValidator pinValidator;
  private final RateLimitService rateLimitService;
  private final AuditLogRepository auditLogRepository;
  private final PasswordEncoder passwordEncoder;

  private static final int MAX_ATTEMPTS = 5;
  private static final int RATE_LIMIT_WINDOW_MINUTES = 60;

  /**
   * Validates PIN for loan application.
   *
   * @param pin The PIN to validate (can be null for optional validation)
   * @param userId The user ID
   * @param ipAddress Client IP for rate limiting
   * @return ValidationResult containing success status and details
   */
  public ValidationResult execute(String pin, UUID userId, String ipAddress) {

    // If no PIN provided, skip validation
    if (pin == null || pin.isEmpty()) {
      log.info("PIN validation skipped for user {} - no PIN provided", userId);
      return ValidationResult.skipped();
    }

    // Check rate limit
    String rateLimitKey = "pin-validation:" + userId;
    if (!rateLimitService.tryConsume(rateLimitKey, MAX_ATTEMPTS, RATE_LIMIT_WINDOW_MINUTES)) {
      log.warn("Rate limit exceeded for PIN validation user {}", userId);
      auditLog(userId, "RATE_LIMIT_EXCEEDED", ipAddress);
      throw new PinValidationException(
          "PIN_VALIDATION_RATE_LIMITED",
          "Too many PIN validation attempts. Please try again later.");
    }

    // Get user and validate
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Check if PIN is set
    if (!Boolean.TRUE.equals(user.getPinSet()) || user.getPin() == null) {
      // Don't reveal whether PIN is set - return generic error
      log.warn("PIN validation failed for user {} - PIN not set", userId);
      auditLog(userId, "VALIDATION_FAILED_PIN_NOT_SET", ipAddress);
      throw new PinValidationException("PIN_VALIDATION_FAILED", "Invalid PIN");
    }

    // Validate PIN against hash
    boolean isValid = passwordEncoder.matches(pin, user.getPin());

    if (isValid) {
      log.info("PIN validation successful for user {}", userId);
      auditLog(userId, "VALIDATION_SUCCESS", ipAddress);

      // Reset failed attempts on success
      user.setFailedLoginAttempts(0);
      user.setLastFailedLoginTime(null);
      userRepository.save(user);

      return ValidationResult.success();
    } else {
      log.warn("PIN validation failed for user {}", userId);
      auditLog(userId, "VALIDATION_FAILED", ipAddress);

      // Increment failed attempts
      user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
      user.setLastFailedLoginTime(LocalDateTime.now());
      userRepository.save(user);

      // Check if account should be blocked
      if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
        log.error("User {} blocked due to too many failed PIN attempts", userId);
        auditLog(userId, "ACCOUNT_BLOCKED", ipAddress);
        throw new PinValidationException(
            "PIN_VALIDATION_BLOCKED",
            "Account temporarily blocked due to too many failed attempts. Please contact support.");
      }

      throw new PinValidationException("PIN_VALIDATION_FAILED", "Invalid PIN");
    }
  }

  private void auditLog(UUID userId, String action, String ipAddress) {
    AuditLog auditLog =
        AuditLog.builder()
            .userId(userId)
            .action("PIN_VALIDATION_" + action)
            .resourceType("PIN")
            .ipAddress(ipAddress)
            .description("PIN validation " + action.toLowerCase())
            .build();
    auditLogRepository.save(auditLog);
  }

  @lombok.Data
  @lombok.Builder
  @lombok.AllArgsConstructor
  public static class ValidationResult {
    private boolean valid;
    private boolean skipped;
    private String message;

    public static ValidationResult success() {
      return ValidationResult.builder()
          .valid(true)
          .skipped(false)
          .message("PIN validated successfully")
          .build();
    }

    public static ValidationResult skipped() {
      return ValidationResult.builder()
          .valid(
              true) // Treat specific skipped case as "valid" to proceed, but check 'skipped' flag
          .skipped(true)
          .message("PIN validation skipped - no PIN provided")
          .build();
    }
  }
}
