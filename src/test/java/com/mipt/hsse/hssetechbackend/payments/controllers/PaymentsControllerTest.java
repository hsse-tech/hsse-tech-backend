package com.mipt.hsse.hssetechbackend.payments.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import com.mipt.hsse.hssetechbackend.payments.controllers.requests.TopUpBalanceRequest;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpBalanceProviderBase;
import com.mipt.hsse.hssetechbackend.payments.providers.TopUpSession;
import com.mipt.hsse.hssetechbackend.payments.services.WalletServiceBase;

import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PaymentsController.class)
@Import({ObjectMapper.class, SecurityConfig.class, MiptOAuth2UserService.class})
class PaymentsControllerTest {
  @MockBean
  private WalletServiceBase walletService;

  @MockBean
  private TopUpBalanceProviderBase topUpBalanceProviderBase;

  @Autowired
  private MockMvc http;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private UserPassportServiceBase passportService;

  @Test
  @WithMockUser
  public void testTopUpShouldBeSuccessful() throws Exception {
    when(walletService.getWalletByOwner(any())).thenReturn(new Wallet());
    when(topUpBalanceProviderBase.topUpBalance(any(), any())).thenReturn(new TopUpSession(true, "https://payment-session.com"));

    var reqJson = objectMapper.writeValueAsString(new TopUpBalanceRequest(100));

    http.perform(
            post("/api/payment/top-up-balance")
                .content(reqJson)
                .contentType(MediaType.APPLICATION_JSON)
                .with(oauth2Login()
                        .authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))
                        .attributes(attrs -> attrs.put(OAuth2UserHelper.INNER_ID_ATTR, "7456c54e-1204-4be9-96d8-c81d57cf1593"))))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  @WithMockUser
  public void testTopUpShouldNotBeSuccessfulBecauseUserNotFound() throws Exception {
    when(walletService.getWalletByOwner(any())).thenThrow(new EntityNotFoundException());
    when(topUpBalanceProviderBase.topUpBalance(any(), any())).thenReturn(new TopUpSession(true, "https://payment-session.com"));

    var reqJson = objectMapper.writeValueAsString(new TopUpBalanceRequest(100));

    http.perform(post("/api/payment/top-up-balance")
                    .content(reqJson)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(oauth2Login()
                            .authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))
                            .attributes(attrs -> attrs.put(OAuth2UserHelper.INNER_ID_ATTR, "7456c54e-1204-4be9-96d8-c81d57cf1593"))))
            .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  public void testTopUpShouldNotBeSuccessfulBecauseSessionInitializationFailed() throws Exception {
    when(walletService.getWalletByOwner(any())).thenReturn(new Wallet());
    when(topUpBalanceProviderBase.topUpBalance(any(), any())).thenReturn(new TopUpSession(false, "https://payment-session.com"));

    var reqJson = objectMapper.writeValueAsString(new TopUpBalanceRequest(100));

    http.perform(post("/api/payment/top-up-balance")
                    .content(reqJson)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(oauth2Login()
                            .authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))
                            .attributes(attrs -> attrs.put(OAuth2UserHelper.INNER_ID_ATTR, "7456c54e-1204-4be9-96d8-c81d57cf1593"))))
            .andExpect(status().isBadRequest());
  }
}