package com.mipt.hsse.hssetechbackend.users.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import com.mipt.hsse.hssetechbackend.users.administation.RolesServiceBase;
import com.mipt.hsse.hssetechbackend.users.controllers.requests.ActivateKeyRequest;
import com.mipt.hsse.hssetechbackend.users.controllers.responses.KeyGenResultResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SuperAdminController.class)
@Import({ObjectMapper.class, SecurityConfig.class, MiptOAuth2UserService.class})
class SuperAdminControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private RolesServiceBase rolesService;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserPassportServiceBase passportService;

  @Test
  @WithMockUser
  public void testSetAdmin() throws Exception {
    mockMvc
        .perform(
            post("/api/sa/admins/3598ed06-db2f-4eab-933c-326c4fc1e827")
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andExpect(status().isOk());

    verify(rolesService).setUserAdmin(eq(UUID.fromString("3598ed06-db2f-4eab-933c-326c4fc1e827")));
  }

  @Test
  @WithMockUser
  public void testRemoveAdmin() throws Exception {
    mockMvc
        .perform(
            delete("/api/sa/admins/3598ed06-db2f-4eab-933c-326c4fc1e827")
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andExpect(status().isOk());

    verify(rolesService).removeAdmin(eq(UUID.fromString("3598ed06-db2f-4eab-933c-326c4fc1e827")));
  }

  @Test
  @WithMockUser
  public void testSuperAdminKeyGeneration() throws Exception {
    when(rolesService.generateSuperAdminActivationKey())
        .thenReturn("3598ed06-db2f-4eab-933c-326c4fc1e827");

    var result =
        mockMvc
            .perform(
                post("/api/sa/keygen")
                    .with(
                        oauth2Login()
                            .authorities(
                                new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"),
                                new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andExpect(status().isOk())
            .andReturn();

    var keyGenResult =
        objectMapper.readValue(
            result.getResponse().getContentAsByteArray(), KeyGenResultResponse.class);

    assertNotNull(keyGenResult);
    assertEquals("3598ed06-db2f-4eab-933c-326c4fc1e827", keyGenResult.key());
  }

  @Test
  @WithMockUser
  public void testSuperAdminActivation() throws Exception {
    var key = "3598ed06-db2f-4eab-933c-326c4fc1e827";
    mockMvc
        .perform(
            post("/api/sa/activate-key")
                .content(objectMapper.writeValueAsString(new ActivateKeyRequest(key)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"),
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"))
                        .attributes(attrs -> attrs.put(OAuth2UserHelper.INNER_ID_ATTR, key))))
        .andExpect(status().isOk())
        .andReturn();

    verify(rolesService).activateSuperAdmin(eq(UUID.fromString(key)), eq(key));
  }
}
