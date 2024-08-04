package com.mipt.hsse.hssetechbackend.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TinkoffRestTemplateConfiguration {
  @Bean("TINKOFF_REST_CLIENT")
  public RestTemplate configureRestTemplate(@Value("${tinkoff.api-base-url}") String tinkoffBaseApiRoute) {
    return new RestTemplateBuilder()
            .rootUri(tinkoffBaseApiRoute)
            .build();
  }
}
