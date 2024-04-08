package com.mipt.hsse.hssetechbackend.rent.exceptions;

public class VerificationFailedException extends RuntimeException {
  public VerificationFailedException() {
  }

  public VerificationFailedException(String message) {
    super(message);
  }

  public VerificationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
