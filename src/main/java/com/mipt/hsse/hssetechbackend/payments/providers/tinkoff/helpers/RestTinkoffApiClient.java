package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class RestTinkoffApiClient implements TinkoffApiClientBase {
  private final RestTemplate restTemplate;

  public RestTinkoffApiClient(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public <T> TinkoffResponse<T> get(String route, TinkoffRequestBase payload, Class<T> responseType) {
    return exchange(route, HttpMethod.GET, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> post(String route, TinkoffRequestBase payload, Class<T> responseType) {
    return exchange(route, HttpMethod.POST, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> delete(String route, TinkoffRequestBase payload, Class<T> responseType) {
    return exchange(route, HttpMethod.DELETE, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> put(String route, TinkoffRequestBase payload, Class<T> responseType) {
    return exchange(route, HttpMethod.PUT, payload, responseType);
  }

  @Override
  public <T> TinkoffResponse<T> patch(String route, TinkoffRequestBase payload, Class<T> responseType) {
    return exchange(route, HttpMethod.PATCH, payload, responseType);
  }

  private  <T> TinkoffResponse<T> exchange(String route, HttpMethod method, TinkoffRequestBase payload, Class<T> responseType) {
    var response = restTemplate.exchange(route, method, new HttpEntity<>(payload), responseType);

    if (!response.hasBody() || !response.getStatusCode().is2xxSuccessful()) {
      return new TinkoffResponse<>(false, null);
    }

    return new TinkoffResponse<>(true, response.getBody());
  }
}
