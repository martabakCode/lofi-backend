package com.lofi.lofiapps.service.impl.validator;

import com.lofi.lofiapps.exception.PinValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PinValidator {

  private static final int MIN_UNIQUE_DIGITS = 2;

  /**
   * Validates PIN strength according to security requirements.
   *
   * @param pin The PIN to validate (4-6 digits)
   * @throws PinValidationException if PIN doesn't meet requirements
   */
  public void validateStrength(String pin) {
    if (pin == null || pin.isEmpty()) {
      throw new PinValidationException("PIN_REQUIRED", "PIN is required");
    }

    // Check length
    if (pin.length() < 4 || pin.length() > 6) {
      throw new PinValidationException("PIN_INVALID_LENGTH", "PIN must be between 4 and 6 digits");
    }

    // Check if only digits
    if (!pin.matches("^\\d+$")) {
      throw new PinValidationException("PIN_INVALID_FORMAT", "PIN must contain only digits");
    }

    // Check for repeated digits (all same)
    if (pin.chars().distinct().count() == 1) {
      throw new PinValidationException("PIN_REPEATED_DIGITS", "PIN cannot contain repeated digits");
    }

    // Check for sequential digits (ascending)
    if (isSequential(pin, true)) {
      throw new PinValidationException(
          "PIN_SEQUENTIAL_DIGITS", "PIN cannot contain sequential ascending digits");
    }

    // Check for sequential digits (descending)
    if (isSequential(pin, false)) {
      throw new PinValidationException(
          "PIN_SEQUENTIAL_DIGITS", "PIN cannot contain sequential descending digits");
    }

    // Check for arithmetic sequences (step > 1)
    if (isArithmeticSequence(pin)) {
      throw new PinValidationException(
          "PIN_SEQUENTIAL_DIGITS", "PIN cannot contain arithmetic sequence patterns");
    }

    // Check minimum unique digits
    if (pin.chars().distinct().count() < MIN_UNIQUE_DIGITS) {
      throw new PinValidationException(
          "PIN_INSUFFICIENT_UNIQUE_DIGITS",
          "PIN must have at least " + MIN_UNIQUE_DIGITS + " unique digits");
    }
  }

  private boolean isSequential(String pin, boolean ascending) {
    for (int i = 0; i < pin.length() - 1; i++) {
      int current = pin.charAt(i) - '0';
      int next = pin.charAt(i + 1) - '0';

      if (ascending) {
        if (next - current != 1) return false;
      } else {
        if (current - next != 1) return false;
      }
    }
    return true;
  }

  private boolean isArithmeticSequence(String pin) {
    if (pin.length() < 3) return false;

    int diff = (pin.charAt(1) - '0') - (pin.charAt(0) - '0');
    if (diff == 0) return false; // Already caught by repeated digits check

    for (int i = 1; i < pin.length(); i++) {
      int current = pin.charAt(i) - '0';
      int prev = pin.charAt(i - 1) - '0';
      if ((current - prev) != diff) return false;
    }
    return true;
  }
}
