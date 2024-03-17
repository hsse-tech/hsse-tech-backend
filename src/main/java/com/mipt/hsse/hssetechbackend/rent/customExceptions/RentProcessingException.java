package com.mipt.hsse.hssetechbackend.rent.customExceptions;

public class RentProcessingException extends RuntimeException {
  public RentProcessingException() {
  }

  public RentProcessingException(String message) {
    super(message);
  }

  public RentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}