package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(ItemController.class)
@Import(ObjectMapper.class)
class ItemControllerTest {
  private static final String BASE_MAPPING = "/api/renting/item";
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 60, false);
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean private ItemService itemService;

  @BeforeEach
  void setupObjectMapper() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void testCreateItemEndpoint() throws Exception {
    final String displayName = "Item name";

    when(itemService.createItem(any())).thenReturn(new Item(displayName, itemType));

    CreateItemRequest request = new CreateItemRequest(displayName, UUID.randomUUID());
    String requestStr = objectMapper.writeValueAsString(request);

    var mvcResponse =
        mockMvc
            .perform(post(BASE_MAPPING).content(requestStr).contentType(MediaType.APPLICATION_JSON))
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
  void testCreateItemEndpointNonExistingItemType() throws Exception {
    when(itemService.createItem(any())).thenThrow(EntityNotFoundException.class);

    CreateItemRequest request = new CreateItemRequest("any name", UUID.randomUUID());
    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc
        .perform(post(BASE_MAPPING).content(requestStr).contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteItemEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();

    mockMvc
        .perform(delete(BASE_MAPPING + "/{itemId}", uuid.toString()))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemService).deleteItem(uuid);
  }

  @Test
  void testGetItemEndpoint() throws Exception {
    final String displayName = "displayName";
    Item item = new Item(displayName, itemType);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));

    MvcResult mvcResult =
        mockMvc
            .perform(
                get(BASE_MAPPING + "/{itemId}?loadRentInfo=false", UUID.randomUUID().toString()))
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
  void testGetItemWithFutureRents() throws Exception {
    final String displayName = "Display name";
    User user = new User("user");
    HumanUserPassport humanUserPassport =
        new HumanUserPassport(123L, "testName", "testLastName", "test@gmail.com", user);
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
            .perform(get(BASE_MAPPING + "/{itemId}?loadRentInfo=true", UUID.randomUUID()))
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
  void testGetItemEndpointAbsentItem() throws Exception {
    when(itemService.getItem(any())).thenReturn(Optional.empty());

    mockMvc
        .perform(get(BASE_MAPPING + "/{itemId}", UUID.randomUUID().toString()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
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
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNoContent());

    verify(itemService).updateItem(uuid, updateRequest);
  }

  @Test
  void testUpdateNonExistingItem() throws Exception {
    doThrow(EntityNotFoundException.class).when(itemService).updateItem(any(), any());

    UpdateItemRequest updateRequest = new UpdateItemRequest("new display name");
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc
        .perform(
            patch(BASE_MAPPING + "/{id}", UUID.randomUUID().toString())
                .content(requestStr)
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  void testCreateQrCodeForItemBooking() throws Exception {
    var initBytes = new byte[] {0, 1, 2, 3};
    when(itemService.getQrCodeForItem(any(), anyInt(), anyInt())).thenReturn(initBytes);

    var mvcResult =
        mockMvc
            .perform(get(BASE_MAPPING + "/{item_id}/qr", UUID.randomUUID().toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    byte[] responseBytes = mvcResult.getContentAsByteArray();
    assertNotNull(responseBytes);
    assertArrayEquals(initBytes, responseBytes);
  }

  @Test
  void testProvideAccessToItemEndpoint() throws Exception {
    UUID itemId = UUID.randomUUID();

    when(itemService.existsById(itemId)).thenReturn(true);

    mockMvc
        .perform(post(BASE_MAPPING + "/{item_id}/try-open", itemId.toString()))
        .andDo(print())
        .andExpect(status().isOk());

    verify(itemService).provideAccessToItem(itemId);
  }

  @Test
  void testProvideAccessToItemIfDenied() throws Exception {
    UUID itemId = UUID.randomUUID();

    when(itemService.existsById(itemId)).thenReturn(false);

    mockMvc
        .perform(post(BASE_MAPPING + "/{item_id}/try-open", itemId.toString()))
        .andDo(print())
        .andExpect(status().isBadRequest());

    verify(itemService, never()).provideAccessToItem(itemId);
  }
}
