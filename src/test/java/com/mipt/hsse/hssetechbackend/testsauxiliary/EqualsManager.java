package com.mipt.hsse.hssetechbackend.testsauxiliary;

import java.time.Duration;
import java.time.Instant;

public class EqualsManager {
  /**
   * Compare Instants with delta of 1 millisecond <br>
   * May be useful when working with {@link Instant#now()}
    */
  public static boolean equalsInstantsWithDelta(Instant a, Instant b) {
    return Duration.between(a, b).toMillis() == 0;
  }
}
