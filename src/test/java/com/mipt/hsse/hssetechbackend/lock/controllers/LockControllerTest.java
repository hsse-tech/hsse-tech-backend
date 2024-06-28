package com.mipt.hsse.hssetechbackend.lock.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.UpdateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.controllers.responses.CreateLockResponse;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemAlreadyHasLockException;
import com.mipt.hsse.hssetechbackend.lock.services.LockServiceBase;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LockController.class)
@Import({ObjectMapper.class, SecurityConfig.class, MiptOAuth2UserService.class})
class LockControllerTest {
  private static final String BASE_MAPPING = "/api/locks";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private LockServiceBase lockService;

  @MockBean private UserPassportServiceBase userPassportService;

  private static OAuth2User commonUserPrincipal;
  private static OAuth2User adminPrincipal;

  @BeforeAll
  static void setupTestUser() {
    UUID commonUserId = UUID.randomUUID();
    UUID adminId = UUID.randomUUID();

    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", "123");
    attributes.put(OAuth2UserHelper.INNER_ID_ATTR, commonUserId);
    commonUserPrincipal = new DefaultOAuth2User(
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_MIPT_USER")),
        attributes,
        "sub");

    Map<String, Object> adminAttributes = new HashMap<>();
    adminAttributes.put("sub", "456");
    adminAttributes.put(OAuth2UserHelper.INNER_ID_ATTR, adminId);
    adminPrincipal = new DefaultOAuth2User(
        List.of(new SimpleGrantedAuthority("ROLE_MIPT_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")),
        adminAttributes,
        "sub");
  }

  @Test
  @WithMockUser
  void testCreateLockEndpoint() throws Exception {
    Item item = new Item("name", new ItemType(BigDecimal.ZERO, "name", 100, false));
    item.setId(UUID.randomUUID());
    UUID lockId = UUID.randomUUID();
    CreateLockRequest request = new CreateLockRequest(item.getId());
    LockPassport lock = new LockPassport(item);
    lock.setId(lockId);

    when(lockService.createLock(any())).thenReturn(lock);

    String requestStr = objectMapper.writeValueAsString(request);

    var mvcResponse =
        mockMvc
            .perform(
                post(BASE_MAPPING)
                    .content(requestStr)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(oauth2Login().oauth2User(adminPrincipal)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    CreateLockResponse response = objectMapper.readValue(mvcResponse, CreateLockResponse.class);

    verify(lockService).createLock(request);

    assertNotNull(response);
    assertEquals(lockId, response.id());
  }

  @Test
  @WithMockUser
  void testDeleteLockEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();

    doNothing().when(lockService).deleteLock(lockId);

    mockMvc
        .perform(
            delete(BASE_MAPPING + "/{id}", lockId.toString())
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(lockService).deleteLock(lockId);
  }

  @Test
  @WithMockUser
  void testUpdateItemUnderLockEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UpdateLockRequest request = new UpdateLockRequest(itemId);

    doNothing().when(lockService).updateItemUnderLock(lockId, itemId);

    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(
            patch(BASE_MAPPING + "/{lock_id}", lockId.toString())
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(lockService).updateItemUnderLock(lockId, itemId);
  }

  @Test
  @WithMockUser
  void testUpdateItemUnderLockEndpointItemAlreadyHasLock() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();
    UpdateLockRequest request = new UpdateLockRequest(itemId);

    doThrow(ItemAlreadyHasLockException.class)
        .when(lockService)
        .updateItemUnderLock(lockId, itemId);

    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(
            patch(BASE_MAPPING + "/{lock_id}", lockId.toString())
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verify(lockService).updateItemUnderLock(lockId, itemId);
  }

  @Test
  @WithMockUser
  void testOpenLockEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();
UUID userId = OAuth2UserHelper.getUserId(commonUserPrincipal);

    when(lockService.canUserOpenLock(userId, lockId)).thenReturn(true);
    doNothing().when(lockService).openLock(lockId);

    mockMvc
        .perform(
            post(BASE_MAPPING + "/{id}/open", lockId.toString())
                .with(oauth2Login().oauth2User(commonUserPrincipal)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(lockService).openLock(lockId);
  }

  @Test
  @WithMockUser
  void testOpenLockEndpointForbidden() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID userId = OAuth2UserHelper.getUserId(commonUserPrincipal);

    when(lockService.canUserOpenLock(userId, lockId)).thenReturn(false);

    mockMvc
        .perform(
            post(BASE_MAPPING + "/{id}/open", lockId.toString())
                .with(oauth2Login().oauth2User(commonUserPrincipal)))
        .andDo(print())
        .andExpect(status().isForbidden());

    verify(lockService, never()).openLock(lockId);
  }

  @Test
  @WithMockUser
  void testIsLockOpenEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();

    when(lockService.isLockOpen(lockId)).thenReturn(true);
    doNothing().when(lockService).closeLock(lockId);

    var mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{id}/is-open", lockId.toString())
                    .with(oauth2Login().oauth2User(commonUserPrincipal)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Boolean isOpen = objectMapper.readValue(mvcResult, Boolean.class);

    assertTrue(isOpen);
    verify(lockService).isLockOpen(lockId);
    verify(lockService).closeLock(lockId);
  }

  @Test
  @WithMockUser
  void testIsLockOpenEndpointClosed() throws Exception {
    UUID lockId = UUID.randomUUID();

    when(lockService.isLockOpen(lockId)).thenReturn(false);

    var mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{id}/is-open", lockId.toString())
                    .with(oauth2Login().oauth2User(commonUserPrincipal)))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    Boolean isOpen = objectMapper.readValue(mvcResult, Boolean.class);

    assertFalse(isOpen);
    verify(lockService).isLockOpen(lockId);
    verify(lockService, never()).closeLock(lockId);
  }
}
