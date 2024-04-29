package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.CreatePaymentSessionTinkoffEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RequestsSignerFullSignTest {
  private static RequestsSigner signer;

  @BeforeAll
  public static void setUp() {
    signer = new RequestsSigner("TINKOFF_TEST_KEY",
            new TinkoffPropsSerializer("TINKOFF_TEST_PASSWORD"));
  }

  @Test
  public void testFullSign() {
    var createSessionRequest = new CreatePaymentSessionTinkoffEntity(100, "123");

    assertNull(createSessionRequest.getTerminalKey());
    assertNull(createSessionRequest.getToken());

    signer.createSign(createSessionRequest, true);

    assertEquals("TINKOFF_TEST_KEY", createSessionRequest.getTerminalKey());
    assertEquals(
            "33a21d1c041a0bd57682acc5462f9b274c4d1493a7e0e2f6ca0fa15e051bdbf9",
            createSessionRequest.getToken());
  }
}
