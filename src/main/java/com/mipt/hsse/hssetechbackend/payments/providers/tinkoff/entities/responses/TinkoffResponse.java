package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses;

import java.util.Optional;

public class TinkoffResponse<T> {
  private final boolean isSuccess;
  private final T payload;

  public TinkoffResponse(boolean isSuccess, T payload) {
    this.isSuccess = isSuccess;
    this.payload = payload;
  }

  public boolean isSuccess() {
    return isSuccess;
  }

  public Optional<T> getPayload() {
    return Optional.ofNullable(payload);
  }
}
