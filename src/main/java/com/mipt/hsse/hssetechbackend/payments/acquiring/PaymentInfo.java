package com.mipt.hsse.hssetechbackend.payments.acquiring;

import java.util.Optional;
import java.util.UUID;

public record PaymentInfo(double amount, UUID orderId, Optional<String> description) {
  public PaymentInfo(double amount, UUID orderId) {
    this(amount, orderId, Optional.empty());
  }
}
