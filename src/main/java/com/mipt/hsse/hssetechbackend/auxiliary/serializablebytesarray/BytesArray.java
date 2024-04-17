package com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Arrays;

/**
 * A wrapper around byte[].class which is not serialized by Jackson<br>
 */
@JsonSerialize(using = BytesSerializer.class)
@JsonDeserialize(using = BytesDeserializer.class)
public record BytesArray(byte[] bytes) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BytesArray that = (BytesArray) o;
    return Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }
}
