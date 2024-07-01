package com.mipt.hsse.hssetechbackend.rent.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason = "Некорректный ввод")
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
