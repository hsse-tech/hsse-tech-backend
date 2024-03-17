package com.mipt.hsse.hssetechbackend.rent.customexceptions;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public <ID> EntityNotFoundException(Class<?> clas, ID id) {
    this("Not found entity of class " + clas.getName() + " with given id: " + id.toString());
  }
}
