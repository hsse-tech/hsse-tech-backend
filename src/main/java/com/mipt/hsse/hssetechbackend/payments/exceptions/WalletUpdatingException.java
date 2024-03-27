package com.mipt.hsse.hssetechbackend.payments.exceptions;

public class WalletUpdatingException extends RuntimeException {
  public WalletUpdatingException(String message) {
    super(message);
  }
}
