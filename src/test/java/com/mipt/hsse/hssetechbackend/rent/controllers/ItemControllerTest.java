package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetShortRentResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
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
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaHumanUserPassportRepository jpaHumanUserPassportRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaRentRepository rentRepository;

  private static final String BASE_MAPPING = "/api/renting/item";

  private ItemType itemType;

  @BeforeEach
  public void setupRestTemplate() {
    rentRepository.deleteAll();
    itemTypeRepository.deleteAll();
    jpaHumanUserPassportRepository.deleteAll();
    rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
  }

  @BeforeEach
  public void createItemType() {
    itemType = itemTypeRepository.save(new ItemType(BigDecimal.ZERO, "Item type name", 60, false));
  }

  @Test
  void testCreateItemEndpoint() {
    final String displayName = "Item name";

    when(itemService.createItem(any())).thenReturn(new Item(displayName, itemType));

    CreateItemRequest request = new CreateItemRequest(displayName, itemType.getId());

    ResponseEntity<Item> createResponse = rest.postForEntity(BASE_MAPPING, request, Item.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    verify(itemService).createItem(request);

    Item responseItem = createResponse.getBody();
    assertNotNull(responseItem);
    assertEquals(displayName, responseItem.getDisplayName());
    assertEquals(itemType.getId(), responseItem.getType().getId());
  }

  @Test
  void testCreateItemEndpointNonExistingItemType() {
    when(itemService.createItem(any())).thenThrow(EntityNotFoundException.class);

    CreateItemRequest request = new CreateItemRequest("any name", UUID.randomUUID());

    ResponseEntity<Item> createResponse = rest.postForEntity(BASE_MAPPING, request, Item.class);

    assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
  }

  @Test
  void testDeleteItemTypeEndpoint() {
    UUID uuid = UUID.randomUUID();
    rest.delete(BASE_MAPPING + "/{itemId}", Map.of("itemId", uuid));

    verify(itemService).deleteItem(uuid);
  }

  @Test
  void testGetItemEndpoint() {
    final String displayName = "displayName";
    Item item = new Item(displayName, itemType);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));

    ResponseEntity<GetItemResponse> responseEntity =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}?loadRentInfo=false",
            GetItemResponse.class,
            Map.of("itemId", UUID.randomUUID()));

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    var response = responseEntity.getBody();
    assertNotNull(response);
    assertEquals(displayName, response.getDisplayName());
    assertEquals(itemType.getId(), response.getTypeId());
    assertNull(response.getRents());
  }

  @Test
  void getItemWithFutureRents() {
    final String displayName = "Display name";

    User user = new User("user");

    HumanUserPassport humanUserPassport = new HumanUserPassport(123L, "testName", "testLastName", "test@gmail.com", user);
    humanUserPassport = jpaHumanUserPassportRepository.save(humanUserPassport);

    Item item = new Item(displayName, itemType);
    item = itemRepository.save(item);

    Rent rent =
        new Rent(
            Instant.now().plus(5, ChronoUnit.DAYS),
            Instant.now().plus(6, ChronoUnit.DAYS),
            humanUserPassport,
            item);
    rentRepository.save(rent);

    when(itemService.getItem(any())).thenReturn(Optional.of(item));
    when(itemService.getFutureRentsOfItem(any())).thenReturn(List.of(rent));

    ResponseEntity<GetItemResponse> responseEntity =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}?loadRentInfo=true",
            GetItemResponse.class,
            Map.of("itemId", UUID.randomUUID()));

    var response = responseEntity.getBody();
    assertNotNull(response);
    assertEquals(displayName, response.getDisplayName());
    assertEquals(itemType.getId(), response.getTypeId());
    assertEquals(1, response.getRents().size());

    GetShortRentResponse retrievedRent = response.getRents().get(0);
    assertEquals(rent.getId(), retrievedRent.id());

    rentRepository.deleteAll();
    userRepository.deleteAll();
    itemRepository.deleteAll();
  }

  @Test
  void testGetItemEndpointAbsentItem() {
    when(itemService.getItem(any())).thenReturn(Optional.empty());

    ResponseEntity<Item> response =
        rest.getForEntity(
            BASE_MAPPING + "/{itemId}", Item.class, Map.of("itemId", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testUpdateItemEndpoint() {
    final String displayName = "displayName";

    when(itemService.getItem(any())).thenReturn(Optional.of(new Item(displayName, itemType)));
    when(itemService.existsById(any())).thenReturn(true);
    doNothing().when(itemService).updateItem(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateItemRequest updateRequest = new UpdateItemRequest(displayName);
    HttpEntity<UpdateItemRequest> requestEntity = new HttpEntity<>(updateRequest);

    ResponseEntity<Void> responseEntity = rest.exchange(
        BASE_MAPPING + "/{id}", HttpMethod.PATCH, requestEntity, void.class, Map.of("id", uuid));

    assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    verify(itemService).updateItem(uuid, updateRequest);
  }

  @Test
  void testUpdateItemTypeEndpointonExistingItem() {
    doNothing().when(itemService).updateItem(any(), any());

    UpdateItemRequest updateRequest = new UpdateItemRequest("new display name");
    HttpEntity<UpdateItemRequest> requestEntity = new HttpEntity<>(updateRequest);

    ResponseEntity<Void> response =
        rest.exchange(
            BASE_MAPPING + "/{id}",
            HttpMethod.PATCH,
            requestEntity,
            void.class,
            Map.of("id", UUID.randomUUID()));

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testCreateQrCodeForItemBooking() {
    ResponseEntity<byte[]> response =
        rest.getForEntity(
            BASE_MAPPING + "/{item_id}/qr", byte[].class, Map.of("item_id", UUID.randomUUID()));

    assertEquals(HttpStatus.OK, response.getStatusCode());

    byte[] imageBytes = response.getBody();
    assertNotNull(imageBytes);
  }
}
