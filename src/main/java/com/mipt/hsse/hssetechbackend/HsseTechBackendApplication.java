package com.mipt.hsse.hssetechbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HsseTechBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(HsseTechBackendApplication.class, args);
  }
}
