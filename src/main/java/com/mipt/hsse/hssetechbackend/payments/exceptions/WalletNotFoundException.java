package com.mipt.hsse.hssetechbackend.payments.exceptions;

public class WalletNotFoundException extends RuntimeException {
  public WalletNotFoundException(String message) {
    super(message);
  }

  public WalletNotFoundException() {
    this("Wallet not found");
  }
}
