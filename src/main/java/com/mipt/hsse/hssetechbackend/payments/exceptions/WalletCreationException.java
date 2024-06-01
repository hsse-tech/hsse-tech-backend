package com.mipt.hsse.hssetechbackend.payments.exceptions;

public class WalletCreationException extends RuntimeException {
  public WalletCreationException(String message) {
    super(message);
  }
}
