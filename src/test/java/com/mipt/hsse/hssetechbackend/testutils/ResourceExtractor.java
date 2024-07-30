package com.mipt.hsse.hssetechbackend.testutils;

import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

public class ResourceExtractor {
  public static byte[] getResourceAsBytes(String resourcePath) throws IOException {
    try (InputStream inputStream = ResourceExtractor.class.getResourceAsStream(resourcePath)) {
      return StreamUtils.copyToByteArray(inputStream);
    }
  }
}
