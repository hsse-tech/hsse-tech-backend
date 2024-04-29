package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TinkoffPropsSerializerTest {
  @SuppressWarnings("FieldMayBeFinal")
  @Getter
  private static class BasicObject extends TinkoffRequestBase {
    private String a = "a";
    private String c = "c";
    private String b = "b";
  }

  @SuppressWarnings("FieldMayBeFinal")
  @Getter
  private static class ObjectWithNestedObjects extends TinkoffRequestBase {
    private String a = "a";
    private String c = "c";
    private String b = "b";
    private BasicObject obj = new BasicObject();
  }

  @SuppressWarnings("FieldMayBeFinal")
  @Getter
  private static class TinkoffExampleObject extends TinkoffRequestBase {
    private int amount = 19200;
    private String orderId = "21090";
    private String description = "Подарочная карта на 1000 рублей";
    private Object data = new Object();
    private Object Receipt = new Object();
  }

  private static TinkoffPropsSerializer serializer;
  
  @BeforeAll
  public static void setUp() {
    serializer = new TinkoffPropsSerializer("usaf8fw8fsw21g");
  }

  @Test
  public void testSerializingBasicObject() {
    var result = serializer.serialize(new BasicObject());

    assertEquals("abcusaf8fw8fsw21g", result);
  }

  @Test
  public void testSerializingObjectWithNestedObjects() {
    var result = serializer.serialize(new ObjectWithNestedObjects());

    assertEquals("abcusaf8fw8fsw21g", result);
  }

  @Test
  public void testSerializingTinkoffExampleObject() {
    var obj = new TinkoffExampleObject();

    obj.setTerminalKey("MerchantTerminalKey");

    var result = serializer.serialize(obj);

    assertEquals("19200Подарочная карта на 1000 рублей21090usaf8fw8fsw21gMerchantTerminalKey", result);
  }
}
