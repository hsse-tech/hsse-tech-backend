package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.sessions;

import com.mipt.hsse.hssetechbackend.payments.providers.SessionParams;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.CreatePaymentSessionTinkoffEntity;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.CreatePaymentSessionTinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.TinkoffApiClientBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TinkoffSessionInitializerTest {
  private TinkoffApiClientBase rest;
  private TinkoffSessionInitializer initializer;

  @BeforeEach
  public void setUp() {
    rest = mock(TinkoffApiClientBase.class);
    initializer = new TinkoffSessionInitializer(rest);
  }

  @Test
  public void testCreatingSession() {
    var testResponse = new CreatePaymentSessionTinkoffResponse(
            true, 0, "TinkoffBankTest",
            "NEW", 3093639567L, "21090",
            140000, "https://securepay.tinkoff.ru/new/fU1ppgqa");

    when(rest.post(
            eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            any()))
        .thenReturn(new TinkoffResponse<>(true, testResponse));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            any());

    assertTrue(response.isSuccess());
    assertEquals("https://securepay.tinkoff.ru/new/fU1ppgqa", response.paymentUrl());
    assertEquals("21090", response.orderId());
  }

  @Test
  public void testCreatingSessionButFailedOnTinkoffSide() {
    var testResponse = new CreatePaymentSessionTinkoffResponse(
            false, 0, "TinkoffBankTest",
            "FAIL", 3093639567L, "21090",
            140000, null);

    when(rest.post(
            eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            any()))
            .thenReturn(new TinkoffResponse<>(true, testResponse));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            any());

    assertFalse(response.isSuccess());
  }

  @Test
  public void testCreatingSessionButFailedOnSomeError() {
    when(rest.post(
              eq("/v2/Init"),
              isA(CreatePaymentSessionTinkoffEntity.class),
              any()))
            .thenReturn(new TinkoffResponse<>(false, null));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            any());

    assertFalse(response.isSuccess());
  }
}