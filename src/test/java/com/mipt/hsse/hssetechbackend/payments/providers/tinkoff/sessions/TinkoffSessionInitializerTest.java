package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.sessions;

import com.mipt.hsse.hssetechbackend.payments.providers.SessionParams;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.CreatePaymentSessionTinkoffEntity;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.CreatePaymentSessionTinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.responses.TinkoffResponse;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.SerializationMode;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.TinkoffApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TinkoffSessionInitializerTest {
  private TinkoffApiClient rest;
  private TinkoffSessionInitializer initializer;

  @BeforeEach
  public void setUp() {
    rest = mock(TinkoffApiClient.class);
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
            eq(SerializationMode.SIGN_SHA256_TOKEN)))
        .thenReturn(new TinkoffResponse<>(true, testResponse));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            eq(SerializationMode.SIGN_SHA256_TOKEN));

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
            eq(SerializationMode.SIGN_SHA256_TOKEN)))
            .thenReturn(new TinkoffResponse<>(true, testResponse));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            eq(SerializationMode.SIGN_SHA256_TOKEN));

    assertFalse(response.isSuccess());
  }

  @Test
  public void testCreatingSessionButFailedOnSomeError() {
    when(rest.post(
            eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            eq(SerializationMode.SIGN_SHA256_TOKEN)))
            .thenReturn(new TinkoffResponse<>(false, null));

    var response = initializer.initialize(new SessionParams(140000, "21090"));

    verify(rest).post(eq("/v2/Init"),
            isA(CreatePaymentSessionTinkoffEntity.class),
            eq(SerializationMode.SIGN_SHA256_TOKEN));

    assertFalse(response.isSuccess());
  }
}