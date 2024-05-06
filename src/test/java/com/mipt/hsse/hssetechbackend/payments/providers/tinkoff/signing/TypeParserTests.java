package com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.entities.requests.TinkoffRequestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TypeParserTests {
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

  private TypeParser parser;

  @BeforeEach
  public void setUp() {
    parser = new TypeParser("usaf8fw8fsw21g");
  }

  @Test
  public void testDetectingSigningNoSign() {
    TypeDescriptor typeDescriptor = parser.generateDescriptor(EntityWithoutSignMark.class);

    assertFalse(typeDescriptor.isNeedSign());
  }

  @Test
  public void testDetectingSigningNeedSign() {
    TypeDescriptor typeDescriptor = parser.generateDescriptor(EntityWithSignMark.class);

    assertTrue(typeDescriptor.isNeedSign());
  }

  @Test
  public void testSerializingBasicObject() {
    TypeDescriptor typeDescriptor = parser.generateDescriptor(BasicObject.class);

    assertEquals("abcusaf8fw8fsw21g", typeDescriptor.serialize(new BasicObject()));
  }

  @Test
  public void testSerializingObjectWithNestedObjects() {
    TypeDescriptor typeDescriptor = parser.generateDescriptor(ObjectWithNestedObjects.class);

    assertEquals("abcusaf8fw8fsw21g", typeDescriptor.serialize(new ObjectWithNestedObjects()));
  }

  @Test
  public void testSerializingTinkoffExampleObject() {
    var obj = new TinkoffExampleObject();

    obj.setTerminalKey("MerchantTerminalKey");

    TypeDescriptor typeDescriptor = parser.generateDescriptor(TinkoffExampleObject.class);

    var result = typeDescriptor.serialize(obj);

    assertEquals("19200Подарочная карта на 1000 рублей21090usaf8fw8fsw21gMerchantTerminalKey", result);
  }
}
