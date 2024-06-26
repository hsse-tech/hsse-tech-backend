package com.mipt.hsse.hssetechbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HsseTechBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(HsseTechBackendApplication.class, args);
  }
}
