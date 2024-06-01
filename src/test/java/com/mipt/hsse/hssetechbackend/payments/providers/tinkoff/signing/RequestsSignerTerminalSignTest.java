package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.CreatePaymentSessionTinkoffEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestsSignerTerminalSignTest {
  private static RequestsSigner signer;

  @BeforeAll
  public static void setUp() {
    signer = new RequestsSigner("TINKOFF_TEST_KEY",
            new TinkoffPropsSerializer("TINKOFF_TEST_PASSWORD"));
  }

  @Test
  public void signRequestOnlyTerminalKey() {
    var createSessionRequest = new CreatePaymentSessionTinkoffEntity(100, "123");

    assertNull(createSessionRequest.getTerminalKey());
    assertNull(createSessionRequest.getToken());

    signer.createSign(createSessionRequest, false);

    assertEquals("TINKOFF_TEST_KEY", createSessionRequest.getTerminalKey());
    assertNull(createSessionRequest.getToken());
  }
}
