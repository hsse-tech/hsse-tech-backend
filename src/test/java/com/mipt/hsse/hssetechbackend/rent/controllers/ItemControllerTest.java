package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.controllers.rent.ItemController;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoNotFoundException;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import com.mipt.hsse.hssetechbackend.testutils.ResourceExtractor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ItemController.class)
@MockBean(UserPassportServiceBase.class)
@TestPropertySource("classpath:application-test.properties")
@Import({SecurityConfig.class, MiptOAuth2UserService.class})
class ItemControllerTest {
  private static final String BASE_MAPPING = "/api/renting/item";
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 60, false);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ItemService itemService;

  @BeforeEach
  void setup() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  @WithMockUser
  void testCreateItemEndpoint() throws Exception {
    final String displayName = "Item name";

    when(itemService.createItem(any())).thenReturn(new Item(displayName, itemType));

    CreateItemRequest request = new CreateItemRequest(displayName, UUID.randomUUID());
    String requestStr = objectMapper.writeValueAsString(request);

    var mvcResponse =
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
            .andReturn()
            .getResponse()
            .getContentAsString();
    Item responseItem = objectMapper.readValue(mvcResponse, Item.class);

    verify(itemService).createItem(request);

    assertNotNull(responseItem);
    assertEquals(displayName, responseItem.getDisplayName());
    assertEquals(itemType.getId(), responseItem.getType().getId());
  }

  @Test
  @WithMockUser
  void testCreateItemEndpointNonExistingItemType() throws Exception {
    when(itemService.createItem(any())).thenThrow(EntityNotFoundException.class);

    CreateItemRequest request = new CreateItemRequest("any name", UUID.randomUUID());
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
  void testPinItemThumbnailValid() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] pngBytes = ResourceExtractor.getResourceAsBytes("/test.png");

    mockMvc
        .perform(
            post(BASE_MAPPING + "/" + uuid + "/photo")
                .content(pngBytes)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemService).saveItemPhoto(eq(uuid), aryEq(pngBytes));
  }

  @Test
  @WithMockUser
  void testPinThumbnailPhotoInvalidType() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] pngBytes = new byte[] {0, 1, 2, 3};

    mockMvc
        .perform(
            post(BASE_MAPPING + "/" + uuid + "/photo")
                .content(pngBytes)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
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
  void testGetThumbnailPhoto() throws Exception {
    byte[] pngBytes = ResourceExtractor.getResourceAsBytes("/test.jpg");

    when(itemService.getItemPhoto(any())).thenReturn(pngBytes);

    UUID uuid = UUID.randomUUID();
    var mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/" + uuid + "/photo")
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    verify(itemService).getItemPhoto(eq(uuid));
    assertArrayEquals(pngBytes, mvcResult);
  }

  @Test
  @WithMockUser
  void testBadRequestOnGetNonExistingPhoto() throws Exception {
    when(itemService.getItemPhoto(any())).thenThrow(PhotoNotFoundException.class);

    mockMvc
        .perform(
            get(BASE_MAPPING + "/" + UUID.randomUUID() + "/photo")
                .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testDeleteItemEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();

    mockMvc
        .perform(
            delete(BASE_MAPPING + "/{itemId}", uuid.toString())
                .with(
                    oauth2Login()
                        .authorities(
                            new SimpleGrantedAuthority("ROLE_MIPT_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemService).deleteItem(uuid);
  }

  @Test
  @WithMockUser
  void testGetItemEndpoint() throws Exception {
    final String displayName = "displayName";
    Item item = new Item(displayName, itemType);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{itemId}?loadRentInfo=false", UUID.randomUUID().toString())
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    GetItemResponse response = objectMapper.readValue(responseBody, GetItemResponse.class);

    assertNotNull(response);
    assertEquals(displayName, response.getDisplayName());
    assertEquals(itemType.getId(), response.getTypeId());
    assertNull(response.getRents());
  }

  @Test
  @WithMockUser
  void testGetItemWithFutureRents() throws Exception {
    final String displayName = "Display name";
    HumanUserPassport humanUserPassport =
        new HumanUserPassport(123L, "testName", "testLastName", "test@gmail.com");
    Item item = new Item(displayName, itemType);

    Rent rent =
        new Rent(
            Instant.now().plus(5, ChronoUnit.DAYS),
            Instant.now().plus(6, ChronoUnit.DAYS),
            humanUserPassport,
            item);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));
    when(itemService.getFutureRentsOfItem(any())).thenReturn(List.of(rent));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{itemId}?loadRentInfo=true", UUID.randomUUID())
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    GetItemResponse response = objectMapper.readValue(responseBody, GetItemResponse.class);

    assertNotNull(response);
    assertEquals(displayName, response.getDisplayName());
    assertEquals(1, response.getRents().size());
  }

  @Test
  @WithMockUser
  void testGetItemEndpointAbsentItem() throws Exception {
    when(itemService.getItem(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(
            get(BASE_MAPPING + "/{itemId}", UUID.randomUUID().toString())
                .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void testUpdateItemEndpoint() throws Exception {
    final String displayName = "displayName";

    when(itemService.getItem(any())).thenReturn(Optional.of(new Item(displayName, itemType)));
    when(itemService.existsById(any())).thenReturn(true);
    doNothing().when(itemService).updateItem(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateItemRequest updateRequest = new UpdateItemRequest(displayName);
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

    verify(itemService).updateItem(uuid, updateRequest);
  }

  @Test
  @WithMockUser
  void testUpdateNonExistingItem() throws Exception {
    doThrow(EntityNotFoundException.class).when(itemService).updateItem(any(), any());

    UpdateItemRequest updateRequest = new UpdateItemRequest("new display name");
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

  //  @Test
  //  @WithMockUser
  //  void testCreateQrCodeMockQr() throws Exception {
  //    var initBytes = new byte[] {0, 1, 2, 3};
  //    when(itemService.getQrCodeForItem(any(), anyInt(), anyInt())).thenReturn(initBytes);
  //
  //    var mvcResult =
  //        mockMvc
  //            .perform(
  //                get(BASE_MAPPING + "/{item_id}/qr", UUID.randomUUID().toString())
  //                    .with(oauth2Login().authorities(new
  // SimpleGrantedAuthority("ROLE_MIPT_USER"))))
  //            .andDo(print())
  //            .andExpect(status().isOk())
  //            .andReturn()
  //            .getResponse();
  //
  //    byte[] responseBytes = mvcResult.getContentAsByteArray();
  //    assertNotNull(responseBytes);
  //    assertArrayEquals(initBytes, responseBytes);
  //  }

  @Test
  @WithMockUser
  void testProvideAccessToItemEndpoint() throws Exception {
    UUID itemId = UUID.randomUUID();

    when(itemService.existsById(itemId)).thenReturn(true);

    mockMvc
        .perform(
            post(BASE_MAPPING + "/{item_id}/try-open", itemId.toString())
                .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemService).provideAccessToItem(itemId);
  }

  @Test
  @WithMockUser
  void testProvideAccessToItemIfDenied() throws Exception {
    UUID itemId = UUID.randomUUID();

    when(itemService.existsById(itemId)).thenReturn(false);

    mockMvc
        .perform(
            post(BASE_MAPPING + "/{item_id}/try-open", itemId.toString())
                .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verify(itemService, never()).provideAccessToItem(itemId);
  }
}
