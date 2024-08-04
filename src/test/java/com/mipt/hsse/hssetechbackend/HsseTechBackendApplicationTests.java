package com.mipt.hsse.hssetechbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class HsseTechBackendApplicationTests extends DatabaseSuite {
  @Test
  void contextLoads() {
  }

}
