package com.mipt.hsse.hssetechbackend.payments.exceptions;

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException(String message) {
    super(message);
  }
}
