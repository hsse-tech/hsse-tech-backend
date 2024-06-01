package com.mipt.hsse.hssetechbackend.rent.exceptions;


public class CreateRentProcessingException extends RentProcessingException {
  public CreateRentProcessingException() {
  }

  public CreateRentProcessingException(String message) {
    super(message);
  }

  public CreateRentProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
