package com.mipt.hsse.hssetechbackend.rent.customExceptions;



public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException() {
  }

  public EntityNotFoundException(String message) {
    super(message);
  }

  public EntityNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public <ID_T> EntityNotFoundException(Class<?> clas, ID_T id) {
    this("Not found entity of class " + clas.getName() + " with given id: " + id.toString());
  }
}
