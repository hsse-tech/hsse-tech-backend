package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TinkoffRequestBase {
  private String terminalKey;
}
