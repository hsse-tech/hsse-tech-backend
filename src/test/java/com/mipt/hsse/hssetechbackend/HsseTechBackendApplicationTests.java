package com.mipt.hsse.hssetechbackend;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

@SpringBootTest
@PropertySource("classpath:application.properties")
class HsseTechBackendApplicationTests extends DatabaseSuite {
  @Test
  @SetEnvironmentVariable(key = "OAUTH_SUCCESS_URL", value = "https://localhost:8080")
  @SetEnvironmentVariable(key = "OAUTH_FAILURE_URL", value = "https://localhost:8080")
  @SetEnvironmentVariable(key = "OAUTH_YANDEX_CLIENT_ID", value = "1234")
  @SetEnvironmentVariable(key = "OAUTH_YANDEX_CLIENT_SECRET", value = "1234")
  @SetEnvironmentVariable(key = "OAUTH_YANDEX_REDIRECT_URI", value = "https://localhost:8080")
  @SetEnvironmentVariable(key = "OAUTH_YANDEX_SUCCESS_URI", value = "https://localhost:8080")
  @SetEnvironmentVariable(key = "OAUTH_YANDEX_FAILURE_URI", value = "https://localhost:8080")
  void contextLoads() {
  }

}
