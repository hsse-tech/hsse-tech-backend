package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

public class TypeParser {
  private final String password;

  public TypeParser(String password) {
    this.password = password;
  }

  public <T> TypeDescriptor generateDescriptor(Class<T> type) {
    return TypeDescriptor.create(type, password);
  }
}
