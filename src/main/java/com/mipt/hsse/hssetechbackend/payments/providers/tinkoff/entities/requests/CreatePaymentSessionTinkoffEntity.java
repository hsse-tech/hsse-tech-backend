package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffProperty;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffSign;
import lombok.Getter;

@TinkoffSign
public final class CreatePaymentSessionTinkoffEntity extends TinkoffRequestBase {
  private final int amount;
  private final String orderId;

  public CreatePaymentSessionTinkoffEntity(int amount, String orderId) {
    this.amount = amount;
    this.orderId = orderId;
  }

  @TinkoffProperty(name = "Amount")
  public int getAmount() {
    return amount;
  }

  @TinkoffProperty(name = "OrderId")
  public String getOrderId() {
    return orderId;
  }
}
