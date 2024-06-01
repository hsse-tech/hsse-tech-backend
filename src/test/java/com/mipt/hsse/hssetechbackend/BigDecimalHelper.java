package com.mipt.hsse.hssetechbackend;

import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;

public class BigDecimalHelper {
  public static void assertEquals(BigDecimal expected, BigDecimal actual) {
    Assertions.assertEquals(0, actual.compareTo(expected));
  }

  public static void assertEquals(int expected, BigDecimal actual) {
    assertEquals(BigDecimal.valueOf(expected), actual);
  }
}
