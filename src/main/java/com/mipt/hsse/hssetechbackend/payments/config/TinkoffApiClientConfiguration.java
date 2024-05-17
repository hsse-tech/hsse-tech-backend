package com.mipt.hsse.hssetechbackend.payments.config;

import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.RestTinkoffApiClient;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.SignableTinkoffApiClient;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.helpers.TinkoffApiClientBase;
import com.mipt.hsse.hssetechbackend.payments.providers.tinkoff.signing.RequestsSignerBase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TinkoffApiClientConfiguration {
  @Bean
  public TinkoffApiClientBase configure(@Qualifier("TINKOFF_REST_CLIENT") RestTemplate tinkoffRestTemplate,
                                        RequestsSignerBase signer) {
    var restClient = new RestTinkoffApiClient(tinkoffRestTemplate);

    return new SignableTinkoffApiClient(restClient, signer);
  }
}
