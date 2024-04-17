package com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BytesArrayTest {
  private final Random random = new Random();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testSerializeDeserialize() throws IOException {
    final int bytesSize = 1024;
    byte[] bytes = new byte[bytesSize];
    random.nextBytes(bytes);

    String serialized = objectMapper.writeValueAsString(bytes);
    byte[] retrievedBytes = objectMapper.readValue(serialized, byte[].class);

    assertArrayEquals(bytes, retrievedBytes);
  }
}
