package com.mipt.hsse.hssetechbackend.configurations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
@ContextConfiguration(classes = {ObjectMapperConfiguration.class})
class ObjectMapperConfigurationTest {
  @Autowired ObjectMapper objectMapper;

  @Test
  void testSerializeTime() throws JsonProcessingException {
    Instant instant = Instant.now();
    Instant retrievedInstant =
        objectMapper.readValue(objectMapper.writeValueAsString(instant), Instant.class);
    Assertions.assertEquals(instant, retrievedInstant);
  }
}
