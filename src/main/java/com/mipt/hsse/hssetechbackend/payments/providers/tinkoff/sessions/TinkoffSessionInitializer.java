package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.sessions;

import com.mipt.hsse.hssetechbackend.payments.providers.AcquiringSessionInitializer;
import com.mipt.hsse.hssetechbackend.payments.providers.SessionData;
import com.mipt.hsse.hssetechbackend.payments.providers.SessionParams;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.CreatePaymentSessionTinkoffEntity;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.CreatePaymentSessionTinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.TinkoffApiClient;

import static com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.SerializationMode.SIGN_SHA256_TOKEN;

public class TinkoffSessionInitializer implements AcquiringSessionInitializer {
  private final TinkoffApiClient tinkoffApi;
  private final static String PAYMENT_SESSION_INIT_ROUTE = "/v2/Init";

  public TinkoffSessionInitializer(TinkoffApiClient apiClient) {
    this.tinkoffApi = apiClient;
  }

  @Override
  public SessionData initialize(SessionParams sessionParams) {
    TinkoffResponse<CreatePaymentSessionTinkoffResponse> response = tinkoffApi.post(
            PAYMENT_SESSION_INIT_ROUTE,
            new CreatePaymentSessionTinkoffEntity(sessionParams.amount(), sessionParams.orderId()),
            SIGN_SHA256_TOKEN);

    if (!response.isSuccess()) {
      return new SessionData(sessionParams.amount(), sessionParams.orderId(), false, null);
    }

    var payload = response.getPayload().orElseThrow();

    return new SessionData(
        payload.amount(),
        payload.orderId(),
        payload.success(),
        payload.paymentUrl()
    );
  }
}
