package com.mipt.hsse.hssetechbackend.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RublesToKopeckConverter {
  private static final BigDecimal KOPECK_COUNT_IN_RUBLE = BigDecimal.valueOf(100);

  public static int convertToKopeck(BigDecimal amount) {
    return amount.multiply(KOPECK_COUNT_IN_RUBLE).intValue();
  }
}
