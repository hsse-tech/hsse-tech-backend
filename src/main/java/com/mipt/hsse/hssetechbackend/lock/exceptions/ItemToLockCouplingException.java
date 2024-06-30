package com.mipt.hsse.hssetechbackend.lock.exceptions;

public class ItemToLockCouplingException extends RuntimeException {
  public ItemToLockCouplingException() {
  }

  public ItemToLockCouplingException(String message) {
    super(message);
  }
}
