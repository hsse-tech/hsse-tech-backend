package com.mipt.hsse.hssetechbackend.lock.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.lock.controllers.responses.CreateLockResponse;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
import com.mipt.hsse.hssetechbackend.lock.services.LockServiceBase;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LockController.class)
@Import({SecurityConfig.class, MiptOAuth2UserService.class})
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

    commonUserPrincipal =
        new DefaultOAuth2User(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_MIPT_USER")),
            Map.of(OAuth2UserHelper.INNER_ID_ATTR, commonUserId),
            OAuth2UserHelper.INNER_ID_ATTR);

    adminPrincipal =
        new DefaultOAuth2User(
            List.of(
                new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")),
            Map.of(OAuth2UserHelper.INNER_ID_ATTR, adminId),
            OAuth2UserHelper.INNER_ID_ATTR);
  }

  @Test
  @WithMockUser
  void testCreateLockEndpoint() throws Exception {
    Item item = new Item("name", new ItemType(BigDecimal.ZERO, "name", 100, false));
    item.setId(UUID.randomUUID());
    UUID lockId = UUID.randomUUID();
    LockPassport lock = new LockPassport();
    lock.addItem(item);
    lock.setId(lockId);

    when(lockService.createLock()).thenReturn(lock);

    var mvcResponse =
        mockMvc
            .perform(post(BASE_MAPPING).with(oauth2Login().oauth2User(adminPrincipal)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    CreateLockResponse response = objectMapper.readValue(mvcResponse, CreateLockResponse.class);

    verify(lockService).createLock();

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
  void testAddItemToLockEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();

    doNothing().when(lockService).addItemToLock(lockId, itemId);

    mockMvc
        .perform(
            patch(
                    BASE_MAPPING + "/{lock_id}/add_item/{item_id}",
                    lockId.toString(),
                    itemId.toString())
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(lockService).addItemToLock(lockId, itemId);
  }

  @Test
  @WithMockUser
  void testAddItemToLockEndpointItemAlreadyHasLock() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();

    doThrow(ItemToLockCouplingException.class).when(lockService).addItemToLock(lockId, itemId);

    mockMvc
        .perform(
            patch(
                    BASE_MAPPING + "/{lock_id}/add_item/{item_id}",
                    lockId.toString(),
                    itemId.toString())
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verify(lockService).addItemToLock(lockId, itemId);
  }

  @Test
  @WithMockUser
  void testRemoveItemFromLockEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();

    doNothing().when(lockService).removeItemFromLock(lockId, itemId);

    mockMvc
        .perform(
            patch(
                    BASE_MAPPING + "/{lock_id}/remove_item/{item_id}",
                    lockId.toString(),
                    itemId.toString())
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(lockService).removeItemFromLock(lockId, itemId);
  }

  @Test
  @WithMockUser
  void testRemoveItemFromLockEndpointItemNotLockedByThisLock() throws Exception {
    UUID lockId = UUID.randomUUID();
    UUID itemId = UUID.randomUUID();

    doThrow(ItemToLockCouplingException.class).when(lockService).removeItemFromLock(lockId, itemId);

    mockMvc
        .perform(
            patch(
                    BASE_MAPPING + "/{lock_id}/remove_item/{item_id}",
                    lockId.toString(),
                    itemId.toString())
                .with(oauth2Login().oauth2User(adminPrincipal)))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verify(lockService).removeItemFromLock(lockId, itemId);
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
  void testIsLockOpenEndpoint() throws Exception {
    UUID lockId = UUID.randomUUID();

    when(lockService.isLockOpen(lockId)).thenReturn(true);
    doNothing().when(lockService).closeLock(lockId);

    var mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{id}/is-open", lockId.toString()))
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
                get(BASE_MAPPING + "/{id}/is-open", lockId.toString()))
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
