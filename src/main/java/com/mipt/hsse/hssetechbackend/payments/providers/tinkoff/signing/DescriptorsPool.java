package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import java.util.HashMap;

public class DescriptorsPool {
  private final String password;
  private final HashMap<Class<?>, TypeDescriptor> cachedDescriptors = new HashMap<>();

  public DescriptorsPool(String password) {
    this.password = password;
  }

  public <T> TypeDescriptor getDescriptor(Class<T> type) {
    if (!cachedDescriptors.containsKey(type)) {
      cachedDescriptors.put(type, TypeDescriptor.create(type, password));
    }

    return cachedDescriptors.get(type);
  }
}
