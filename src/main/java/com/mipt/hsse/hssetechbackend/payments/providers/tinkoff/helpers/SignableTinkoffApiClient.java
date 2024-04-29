package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffSign;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.RequestsSignerBase;
import org.springframework.stereotype.Service;

@Service
public class SignableTinkoffApiClient implements TinkoffApiClientBase {
  private final TinkoffApiClientBase http;
  private final RequestsSignerBase requestsSigner;

  public SignableTinkoffApiClient(TinkoffApiClientBase restClientOriginal, RequestsSignerBase requestsSigner) {
    this.http = restClientOriginal;
    this.requestsSigner = requestsSigner;
  }

  @Override
  public <T> TinkoffResponse<T> get(String route, TinkoffRequestBase payload) {
    sign(payload);
    return http.get(route, payload);
  }

  @Override
  public <T> TinkoffResponse<T> post(String route, TinkoffRequestBase payload) {
    sign(payload);
    return http.post(route, payload);
  }

  @Override
  public <T> TinkoffResponse<T> delete(String route, TinkoffRequestBase payload) {
    sign(payload);
    return http.delete(route, payload);
  }

  @Override
  public <T> TinkoffResponse<T> put(String route, TinkoffRequestBase payload) {
    sign(payload);
    return http.put(route, payload);
  }

  @Override
  public <T> TinkoffResponse<T> patch(String route, TinkoffRequestBase payload) {
    sign(payload);
    return http.patch(route, payload);
  }

  private void sign(TinkoffRequestBase payload) {
    var type = payload.getClass();

    var hasSignAttr = type.getAnnotation(TinkoffSign.class) != null;

    requestsSigner.createSign(payload, hasSignAttr);
  }
}
