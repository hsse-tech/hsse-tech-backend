package com.mipt.hsse.hssetechbackend.rent.exceptions;

public class DeleteRentProcessingException extends RentProcessingException {
  public DeleteRentProcessingException() {
  }

  public DeleteRentProcessingException(String message) {
    super(message);
  }

  public DeleteRentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
