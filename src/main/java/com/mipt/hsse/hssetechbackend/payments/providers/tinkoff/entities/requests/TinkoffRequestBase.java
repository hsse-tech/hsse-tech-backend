package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
public abstract class TinkoffRequestBase {
  @JsonInclude(NON_NULL)
  private String terminalKey;

  @JsonInclude(NON_NULL)
  private String token;
}
