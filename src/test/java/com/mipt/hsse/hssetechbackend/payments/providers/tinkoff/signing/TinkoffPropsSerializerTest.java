package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TinkoffPropsSerializerTest {
  private static class EntityWithoutSignMark extends TinkoffRequestBase {

  }

  @TinkoffSign
  private static class EntityWithSignMark extends TinkoffRequestBase {

  }

  @SuppressWarnings("FieldMayBeFinal")
  private static class BasicObject extends TinkoffRequestBase {

    @TinkoffProperty(name = "A")
    public String getA() {
      return "a";
    }

    @TinkoffProperty(name = "C")
    public String getC() {
      return "c";
    }

    @TinkoffProperty(name = "B")
    public String getB() {
      return "b";
    }
  }

  @SuppressWarnings("FieldMayBeFinal")
  private static class ObjectWithNestedObjects extends TinkoffRequestBase {
    @TinkoffProperty(name = "A")
    public String getA() {
      return "a";
    }

    @TinkoffProperty(name = "C")
    public String getC() {
      return "c";
    }

    @TinkoffProperty(name = "B")
    public String getB() {
      return "b";
    }

    @TinkoffProperty(name = "Obj")
    public Object getObj() {
      return new BasicObject();
    }
  }

  @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
  private static class TinkoffExampleObject extends TinkoffRequestBase {
    private int amount = 19200;
    private String orderId = "21090";
    private String description = "Подарочная карта на 1000 рублей";
    private Object data = new Object();
    private Object Receipt = new Object();

    @TinkoffProperty(name = "Amount")
    public int getAmount() {
      return amount;
    }

    @SuppressWarnings("unused")
    @TinkoffProperty(name = "OrderId")
    public String getOrderId() {
      return orderId;
    }

    @TinkoffProperty(name = "Description")
    public String getDescription() {
      return description;
    }

    @TinkoffProperty(name = "Data")
    public Object getData() {
      return data;
    }

    @TinkoffProperty(name = "Receipt")
    public Object getReceipt() {
      return Receipt;
    }
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
