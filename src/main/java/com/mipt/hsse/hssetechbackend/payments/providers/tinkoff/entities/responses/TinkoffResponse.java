package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
public class TinkoffResponse<T> {
  @Getter
  private final boolean isSuccess;

  private final T payload;

  public Optional<T> getPayload() {
    return Optional.ofNullable(payload);
  }
}
