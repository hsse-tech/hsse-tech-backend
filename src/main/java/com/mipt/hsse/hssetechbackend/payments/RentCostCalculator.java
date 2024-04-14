package com.mipt.hsse.hssetechbackend.payments;

import com.mipt.hsse.hssetechbackend.data.entities.Item;

import java.math.BigDecimal;
import java.time.Instant;

public class RentCostCalculator {
  private static final float HOUR_SECONDS = 60 * 60;

  public static BigDecimal calculate(Item item, Instant from, Instant to) {
    var targetItemType = item.getType();
    var costByHour = targetItemType.getCost();
    float rentTimeHour = (to.getEpochSecond() - from.getEpochSecond()) / HOUR_SECONDS;
    return costByHour.multiply(BigDecimal.valueOf(rentTimeHour));
  }
}
