package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.TinkoffProperty;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
public abstract class TinkoffRequestBase {
  @JsonInclude(NON_NULL)
  private String terminalKey;

  @JsonInclude(NON_NULL)
  private String token;

  @TinkoffProperty(name ="TerminalKey")
  public String getTerminalKey() {
    return terminalKey;
  }

  @TinkoffProperty(name ="Token")
  public String getToken() {
    return token;
  }
}
