package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemControllerTest extends DatabaseSuite {
  @Autowired private TestRestTemplate rest;

  @MockBean private ItemService itemService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;

  private static final String BASE_MAPPING = "/api/renting/item";

  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 60, false);

  @BeforeEach
  public void setup() {
    rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
  }

  @BeforeEach
  public void createItemType() {
    itemTypeRepository.save(itemType);
  }

  @Test
  void testCreateItemEndpoint() {
    final String displayName = "Item name";

    when(itemService.createItem(any()))
        .thenReturn(new Item(displayName, itemType));

    CreateItemRequest request =
        new CreateItemRequest(displayName, itemType.getId());

    ResponseEntity<Item> createResponse =
        rest.postForEntity(BASE_MAPPING, request, Item.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    verify(itemService).createItem(request);

    Item responseItem = createResponse.getBody();
    assertNotNull(responseItem);
    assertEquals(displayName, responseItem.getDisplayName());
    assertEquals(itemType.getId(), responseItem.getType().getId());
  }

  @Test
  void testCreateItemOfAbsentTypeEndpoint() {
    final String displayName = "Item name";

    when(itemService.createItem(any()))
        .thenThrow(EntityNotFoundException.class);

    CreateItemRequest request =
        new CreateItemRequest(displayName, itemType.getId());

    ResponseEntity<Item> createResponse =
        rest.postForEntity(BASE_MAPPING, request, Item.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
  }

  // Note that the behaviour of deleteItem endpoint in the case of non-existing item
  // does not differ in any way from the case of existing item on the Controller level
  @Test
  void testDeleteItemTypeEndpoint() {
    doNothing().when(itemService).deleteItem(any());

    UUID uuid = UUID.randomUUID();
    rest.delete(BASE_MAPPING + "/{itemId}", Map.of("itemTypeId", uuid));

    verify(itemService).deleteItem(uuid);
  }

  @Test
  void testGetItemEndpoint() {
    Item item = new Item("display name", itemType);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));

    ResponseEntity<Item> response =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}",
            Item.class,
            Map.of("itemId", UUID.randomUUID()));

    Item returnedItem = response.getBody();
    assertNotNull(returnedItem);
    assertEquals(itemType.getDisplayName(), returnedItem.getDisplayName());
    assertEquals(itemType.getId(), returnedItem.getType().getId());
  }

  @Test
  void testGetItemEndpointOnAbsentItemReturnBadRequest() {
    when(itemService.getItem(any())).thenReturn(Optional.empty());

    ResponseEntity<Item> response =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}",
            Item.class,
            Map.of("itemId", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testUpdateItemEndpoint() {
    final String displayName = "displayName";

    when(itemService.getItem(any()))
        .thenReturn(
            Optional.of(new Item(displayName, itemType)));
    doNothing().when(itemService).updateItem(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateItemRequest updateRequest =
        new UpdateItemRequest(displayName);
    HttpEntity<UpdateItemRequest> requestEntity = new HttpEntity<>(updateRequest);

    rest.exchange(
        BASE_MAPPING + "/{id}", HttpMethod.PATCH, requestEntity, void.class, Map.of("id", uuid));

    verify(itemService).updateItem(uuid, updateRequest);
  }

  @Test
  void testUpdateItemTypeEndpointOnUpdateNonExistReturnBadRequest() {
    doNothing().when(itemService).updateItem(any(), any());

    UpdateItemRequest updateRequest =
        new UpdateItemRequest("new display name");
    HttpEntity<UpdateItemRequest> requestEntity = new HttpEntity<>(updateRequest);

    ResponseEntity<Void> response = rest.exchange(
        BASE_MAPPING + "/{id}", HttpMethod.PATCH, requestEntity, void.class, Map.of("id", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
