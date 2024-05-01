package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffSign;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.RequestsSignerBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SignableTinkoffApiClientTest {
  private static class EntityWithoutSignMark extends TinkoffRequestBase {

  }

  @TinkoffSign
  private static class EntityWithSignMark extends TinkoffRequestBase {

  }

  private static TinkoffApiClientBase apiClientBase;

  private static RequestsSignerBase requestsSigner;

  private static SignableTinkoffApiClient tinkoffApiClient;

  @BeforeAll
  public static void setUp() {
    requestsSigner = mock(RequestsSignerBase.class);
    apiClientBase = mock(TinkoffApiClientBase.class);
    tinkoffApiClient = new SignableTinkoffApiClient(apiClientBase, requestsSigner);
  }

  @Test
  void testRequestsWithoutSignMark() {
    var entity = new EntityWithoutSignMark();

    tinkoffApiClient.get("test", entity, Object.class);
    tinkoffApiClient.post("test", entity, Object.class);
    tinkoffApiClient.delete("test", entity, Object.class);
    tinkoffApiClient.put("test", entity, Object.class);
    tinkoffApiClient.patch("test", entity, Object.class);

    verify(requestsSigner, times(5)).createSign(eq(entity), eq(false));
    verify(apiClientBase, times(1)).get(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).post(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).delete(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).put(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).patch(eq("test"), eq(entity), any());
  }

  @Test
  void testRequestsWithSignMark() {
    var entity = new EntityWithSignMark();

    tinkoffApiClient.get("test", entity, Object.class);
    tinkoffApiClient.post("test", entity, Object.class);
    tinkoffApiClient.delete("test", entity, Object.class);
    tinkoffApiClient.put("test", entity, Object.class);
    tinkoffApiClient.patch("test", entity, Object.class);

    verify(requestsSigner, times(5)).createSign(eq(entity), eq(true));
    verify(apiClientBase, times(1)).get(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).post(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).delete(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).put(eq("test"), eq(entity), any());
    verify(apiClientBase, times(1)).patch(eq("test"), eq(entity), any());
  }
}
