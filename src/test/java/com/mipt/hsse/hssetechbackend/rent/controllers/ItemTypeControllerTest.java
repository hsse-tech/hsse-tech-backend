package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
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
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemTypeControllerTest extends DatabaseSuite {
  @Autowired private TestRestTemplate rest;
  @MockBean private ItemTypeService itemTypeService;

  private static final String BASE_MAPPING = "/api/renting/item-type";

  @BeforeEach
  public void setup() {
    rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
  }

  @Test
  void testCreateItemTypeEndpoint() {
    final BigDecimal cost = BigDecimal.valueOf(100);
    final String displayName = "displayName";
    final int maxRentTime = 60;
    final boolean isPhotoConfirmRequired = true;

    when(itemTypeService.createItemType(any()))
        .thenReturn(new ItemType(cost, displayName, maxRentTime, isPhotoConfirmRequired));

    CreateItemTypeRequest request =
        new CreateItemTypeRequest(cost, displayName, maxRentTime, isPhotoConfirmRequired);

    ResponseEntity<ItemType> createResponse =
        rest.postForEntity(BASE_MAPPING, request, ItemType.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    verify(itemTypeService).createItemType(request);

    ItemType responseItemType = createResponse.getBody();
    assertNotNull(responseItemType);
    assertEquals(cost, responseItemType.getCost());
    assertEquals(displayName, responseItemType.getDisplayName());
    assertEquals(maxRentTime, responseItemType.getMaxRentTimeMinutes());
    assertEquals(isPhotoConfirmRequired, responseItemType.isPhotoRequiredOnFinish());
  }

  @Test
  void testCreateItemTypeEndpointOnInvalidReturnBadRequest() {
    when(itemTypeService.createItemType(any())).thenReturn(null);

    CreateItemTypeRequest request =
        new CreateItemTypeRequest(BigDecimal.valueOf(-100.5), "", null, false);
    ResponseEntity<ItemType> createResponse =
        rest.postForEntity(BASE_MAPPING, request, ItemType.class);
    assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
  }

  @Test
  void testGetItemTypeEndpoint() {
    ItemType itemType = new ItemType(BigDecimal.ZERO, "testName", 60, false);

    when(itemTypeService.getItemType(any())).thenReturn(Optional.of(itemType));

    ResponseEntity<ItemType> response =
        rest.getForEntity(
            BASE_MAPPING + "/{itemTypeId}",
            ItemType.class,
            Map.of("itemTypeId", UUID.randomUUID()));

    ItemType returnedItemType = response.getBody();
    assertNotNull(returnedItemType);
    assertEquals(itemType.getDisplayName(), returnedItemType.getDisplayName());
    assertEquals(itemType.getCost(), returnedItemType.getCost());
    assertEquals(itemType.getMaxRentTimeMinutes(), returnedItemType.getMaxRentTimeMinutes());
  }

  @Test
  void testGetItemTypeEndpointOnGetNonExistReturnBadRequest() {
    when(itemTypeService.getItemType(any())).thenReturn(Optional.empty());

    ResponseEntity<ItemType> response =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}", ItemType.class, Map.of("itemId", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testUpdateItemTypeEndpoint() {
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
    HttpEntity<UpdateItemTypeRequest> requestEntity = new HttpEntity<>(updateRequest);

    rest.exchange(
        BASE_MAPPING + "/{id}", HttpMethod.PATCH, requestEntity, void.class, Map.of("id", uuid));

    verify(itemTypeService).updateItemType(uuid, updateRequest);
  }

  @Test
  void testUpdateItemTypeEndpointOnUpdateNonExistReturnBadRequest() {
    doNothing().when(itemTypeService).updateItemType(any(), any());

    UpdateItemTypeRequest updateRequest =
        new UpdateItemTypeRequest("name", BigDecimal.ZERO, false, 60);
    HttpEntity<UpdateItemTypeRequest> requestEntity = new HttpEntity<>(updateRequest);

    ResponseEntity<Void> response =
        rest.exchange(
            BASE_MAPPING + "/{id}",
            HttpMethod.PATCH,
            requestEntity,
            void.class,
            Map.of("id", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  // Note that the behaviour of deleteItemType endpoint in the case of non-existing item type
  // does not differ in any way from the case of existing item type on the Controller level
  @Test
  void testDeleteItemTypeEndpoint() {
    doNothing().when(itemTypeService).deleteItemType(any());

    UUID uuid = UUID.randomUUID();
    rest.delete(BASE_MAPPING + "/{itemTypeId}", Map.of("itemTypeId", uuid));

    verify(itemTypeService).deleteItemType(uuid);
  }
}
