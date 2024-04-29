package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;

public interface RequestsSignerBase {
  void createSign(TinkoffRequestBase tinkoffRequest, boolean needSha256Sign);
}
