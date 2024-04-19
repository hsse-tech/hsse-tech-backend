package com.mipt.hsse.hssetechbackend.payments.exceptions;

public class TransactionNotFoundException extends RuntimeException {
  public TransactionNotFoundException(String message) {
    super(message);
  }

  public TransactionNotFoundException() {
    this("Transaction for status update not found");
  }
}
