package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TinkoffPropsSerializer {
  private final DescriptorsPool descriptorsPool;

  public TinkoffPropsSerializer(
          @Value("#{environment.getProperty('TINKOFF_PASSWORD')}") String password) {
    descriptorsPool = new DescriptorsPool(password);
  }

  public String serialize(Object object) {
    var descriptor = descriptorsPool.getDescriptor(object.getClass());
    return descriptor.serialize(object);
  }
}
