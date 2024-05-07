package com.mipt.hsse.hssetechbackend.payments.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TopUpBalanceRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpBalanceProviderBase;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpSession;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentsController.class)
@Import(ObjectMapper.class)
class PaymentsControllerTest {
  @MockBean
  private WalletServiceBase walletService;

  @MockBean
  private TopUpBalanceProviderBase topUpBalanceProviderBase;

  @Autowired
  private MockMvc http;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  public void testTopUpShouldBeSuccessful() throws Exception {
    when(walletService.getWalletByOwner(any())).thenReturn(new Wallet());
    when(topUpBalanceProviderBase.topUpBalance(any(), any())).thenReturn(new TopUpSession(true, "https://payment-session.com"));

    var reqJson = objectMapper.writeValueAsString(new TopUpBalanceRequest(UUID.randomUUID(), 100));

    http.perform(post("/api/payment/top-up-balance").content(reqJson).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is3xxRedirection());
  }
}