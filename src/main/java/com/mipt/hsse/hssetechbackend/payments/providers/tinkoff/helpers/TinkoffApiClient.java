package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;

/**
 * REST-клиент для Tinkoff API, который поддерживает <a href="https://www.tinkoff.ru/kassa/dev/payments/#section/Podpis-zaprosa">подпись запроса</a>.
 */
public interface TinkoffApiClient {
  <T> TinkoffResponse<T> get(String route, TinkoffRequestBase payload, SerializationMode serializationMode);
  <T> TinkoffResponse<T> post(String route, TinkoffRequestBase payload, SerializationMode serializationMode);
  <T> TinkoffResponse<T> delete(String route, TinkoffRequestBase payload, SerializationMode serializationMode);
  <T> TinkoffResponse<T> put(String route, TinkoffRequestBase payload, SerializationMode serializationMode);
  <T> TinkoffResponse<T> patch(String route, TinkoffRequestBase payload, SerializationMode serializationMode);
}
