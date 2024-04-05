package com.mipt.hsse.hssetechbackend.rent.exceptions;

public class EntityNotFoundException extends jakarta.persistence.EntityNotFoundException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(Exception cause) {
    super(cause);
  }

  public EntityNotFoundException(String message) {
    super(message);
  }

  public <ID> EntityNotFoundException(Class<?> clas, ID id) {
    super("Not found entity of class " + clas.getName() + " with given id: " + id.toString());
  }
}
