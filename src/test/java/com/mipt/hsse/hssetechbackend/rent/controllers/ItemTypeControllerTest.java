package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.UniqueConstraintViolationException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ItemTypeController.class)
@Import({SecurityConfig.class, MiptOAuth2UserService.class})
class ItemTypeControllerTest {
  private static final String BASE_MAPPING = "/api/renting/item-type";

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ItemTypeService itemTypeService;

  @MockBean private UserPassportServiceBase passportService;

  @Test
  @WithMockUser
  void testCreateItemTypeEndpoint() throws Exception {
    final BigDecimal cost = BigDecimal.valueOf(100);
    final String displayName = "displayName";
    final int maxRentTime = 60;
    final boolean isPhotoConfirmRequired = true;

    when(itemTypeService.createItemType(any()))
        .thenReturn(new ItemType(cost, displayName, maxRentTime, isPhotoConfirmRequired));

    CreateItemTypeRequest request =
        new CreateItemTypeRequest(cost, displayName, maxRentTime, isPhotoConfirmRequired);
    String requestStr = objectMapper.writeValueAsString(request);

    MvcResult mvcResult =
        mockMvc
            .perform(
                post(BASE_MAPPING)
                    .content(requestStr)
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(
                        oauth2Login()
                            .authorities(
                                new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    ItemType responseItemType = objectMapper.readValue(responseBody, ItemType.class);

    assertNotNull(responseItemType);
    assertEquals(cost, responseItemType.getCost());
    assertEquals(displayName, responseItemType.getDisplayName());
    assertEquals(maxRentTime, responseItemType.getMaxRentTimeMinutes());
    assertEquals(isPhotoConfirmRequired, responseItemType.isPhotoRequiredOnFinish());

    verify(itemTypeService).createItemType(request);
  }

  @Test
  @WithMockUser
  void testCreateItemTypeEndpointOnInvalidRequestReturnBadRequest() throws Exception {
    CreateItemTypeRequest request =
        new CreateItemTypeRequest(BigDecimal.valueOf(-100.5), "", null, false);
    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(
            post(BASE_MAPPING)
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testCreateItemTypeEndpointOnDuplicatedNameReturnBadRequest() throws Exception {
    when(itemTypeService.createItemType(any())).thenThrow(UniqueConstraintViolationException.class);

    CreateItemTypeRequest request =
        new CreateItemTypeRequest(BigDecimal.valueOf(100), "name", 100, true);
    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(
            post(BASE_MAPPING)
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testGetItemTypeEndpoint() throws Exception {
    ItemType itemType = new ItemType(BigDecimal.ZERO, "testName", 60, false);

    when(itemTypeService.getItemType(any())).thenReturn(Optional.of(itemType));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{itemTypeId}", UUID.randomUUID().toString())
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    ItemType returnedItemType = objectMapper.readValue(responseBody, ItemType.class);

    assertNotNull(returnedItemType);
    assertEquals(itemType.getDisplayName(), returnedItemType.getDisplayName());
    assertEquals(itemType.getCost(), returnedItemType.getCost());
    assertEquals(itemType.getMaxRentTimeMinutes(), returnedItemType.getMaxRentTimeMinutes());
  }

  @Test
  @WithMockUser
  void testGetItemTypeEndpointOnGetNonExistReturnBadRequest() throws Exception {
    when(itemTypeService.getItemType(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get(BASE_MAPPING + "/{itemId}", UUID.randomUUID().toString())
                .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testUpdateItemTypeEndpoint() throws Exception {
    final BigDecimal cost = BigDecimal.valueOf(100);
    final String displayName = "displayName";
    final Integer maxRentTime = null;
    final boolean isPhotoConfirmRequired = true;

    when(itemTypeService.getItemType(any()))
        .thenReturn(
            Optional.of(new ItemType(cost, displayName, maxRentTime, isPhotoConfirmRequired)));
    doNothing().when(itemTypeService).updateItemType(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateItemTypeRequest updateRequest =
        new UpdateItemTypeRequest(displayName, cost, isPhotoConfirmRequired, maxRentTime);
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc
        .perform(
            patch(BASE_MAPPING + "/{id}", uuid.toString())
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(itemTypeService).updateItemType(uuid, updateRequest);
  }

  @Test
  @WithMockUser
  void testUpdateItemTypeEndpointOnUpdateNonExistReturnBadRequest() throws Exception {
    doThrow(EntityNotFoundException.class).when(itemTypeService).updateItemType(any(), any());

    UpdateItemTypeRequest updateRequest =
        new UpdateItemTypeRequest("name", BigDecimal.ZERO, false, 60);
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc
        .perform(
            patch(BASE_MAPPING + "/{id}", UUID.randomUUID().toString())
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testDeleteItemTypeEndpoint() throws Exception {
    doNothing().when(itemTypeService).deleteItemType(any());

    UUID uuid = UUID.randomUUID();

    mockMvc
        .perform(
            delete(BASE_MAPPING + "/{itemTypeId}", uuid.toString())
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemTypeService).deleteItemType(uuid);
  }
}
