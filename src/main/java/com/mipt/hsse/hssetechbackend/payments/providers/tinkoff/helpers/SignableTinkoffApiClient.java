package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffSign;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.RequestsSignerBase;

/**
 * Представляет собой обертку над обычным Tinkoff API клиентом, который просто подписывает запросы.
 */
public class SignableTinkoffApiClient implements TinkoffApiClientBase {
  private final TinkoffApiClientBase http;
  private final RequestsSignerBase requestsSigner;

  public SignableTinkoffApiClient(TinkoffApiClientBase clientOriginal, RequestsSignerBase requestsSigner) {
    this.http = clientOriginal;
    this.requestsSigner = requestsSigner;
  }

  @Override
  public <T> TinkoffResponse<T> get(String route, TinkoffRequestBase payload, Class<T> responseType) {
    sign(payload);
    return http.get(route, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> post(String route, TinkoffRequestBase payload, Class<T> responseType) {
    sign(payload);
    return http.post(route, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> delete(String route, TinkoffRequestBase payload, Class<T> responseType) {
    sign(payload);
    return http.delete(route, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> put(String route, TinkoffRequestBase payload, Class<T> responseType) {
    sign(payload);
    return http.put(route, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> patch(String route, TinkoffRequestBase payload, Class<T> responseType) {
    sign(payload);
    return http.patch(route, payload, responseType);
  }

  private void sign(TinkoffRequestBase payload) {
    var type = payload.getClass();

    var hasSignAttr = type.getAnnotation(TinkoffSign.class) != null;

    requestsSigner.createSign(payload, hasSignAttr);
  }
}
