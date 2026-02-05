package com.lofi.lofiapps.exception;

public class PinValidationException extends RuntimeException {
  private final String errorCode;

  public PinValidationException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
