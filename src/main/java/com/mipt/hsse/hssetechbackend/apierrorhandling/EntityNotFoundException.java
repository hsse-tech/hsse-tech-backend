package com.mipt.hsse.hssetechbackend.apierrorhandling;

import com.mipt.hsse.hssetechbackend.data.entities.*;
import lombok.Getter;

import java.util.UUID;

@Getter
public class EntityNotFoundException extends RuntimeException {
  private final Class<?> type;

  public EntityNotFoundException(Exception cause, Class<?> type) {
    super(cause);
    this.type = type;
  }

  public EntityNotFoundException(String message, Class<?> type) {
    super(message);
    this.type = type;
  }

  public <ID> EntityNotFoundException(Class<?> type, ID id) {
    this("Not found entity of class " + type.getName() + " with given id: " + id.toString(), type);
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

  public static EntityNotFoundException rentNotFound(UUID id) {
    return new EntityNotFoundException(Rent.class, id);
  }

  public static EntityNotFoundException itemNotFound(UUID id) {
    return new EntityNotFoundException(Item.class, id);
  }

  public static EntityNotFoundException itemTypeNotFound(UUID id) {
    return new EntityNotFoundException(ItemType.class, id);
  }
}
