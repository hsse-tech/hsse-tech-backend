package com.mipt.hsse.hssetechbackend.apierrorhandling;

import com.mipt.hsse.hssetechbackend.data.entities.HumanUserPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Transaction;
import com.mipt.hsse.hssetechbackend.data.entities.Wallet;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(Exception cause) {
    super(cause);
  }

  public EntityNotFoundException(String message) {
    super(message);
  }

  public <ID> EntityNotFoundException(Class<?> type, ID id) {
    super("Not found entity of class " + type.getName() + " with given id: " + id.toString());
  }

  public static EntityNotFoundException userNotFound(UUID id) {
    return new EntityNotFoundException(HumanUserPassport.class, id);
  }

  public static EntityNotFoundException transactionNotFound(UUID id) {
    return new EntityNotFoundException(Transaction.class, id);
  }

  public static EntityNotFoundException walletNotFound(UUID id) {
    return new EntityNotFoundException(Wallet.class, id);
  }
}
