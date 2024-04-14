package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.PinPhotoConfirmationRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.CreateRentResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
class RentControllerTest extends DatabaseSuite {
  @Autowired private TestRestTemplate rest;

  @MockBean private RentService rentService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaUserRepository userRepository;
  @Autowired private JpaHumanUserPassportRepository jpaHumanUserPassportRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaRentRepository rentRepository;

  private static final String BASE_MAPPING = "/api/renting/rent";

  private Item item;
  private HumanUserPassport userPassport;

  @BeforeEach
  public void setupRestTemplate() {
    rest.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
  }

  @BeforeEach
  public void createTestObjects() {
    rentRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    jpaHumanUserPassportRepository.deleteAll();
    userRepository.deleteAll();


    ItemType itemType = itemTypeRepository.save(new ItemType(BigDecimal.ZERO, "Item type name", 120, false));
    item = itemRepository.save(new Item("Item name", itemType));

    User user = new User("human");
    userPassport =
        jpaHumanUserPassportRepository.save(
            new HumanUserPassport(123L, "Name", "Surname", "email@gmail.com", user));
  }

  @Test
  void testCreateRentEndpoint() {
    Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

    when(rentService.createRent(any())).thenReturn(new Rent(start, end, userPassport, item));

    CreateRentRequest createRentRequest = new CreateRentRequest(userPassport.getId(), item.getId(), start, end);

    ResponseEntity<CreateRentResponse> createResponse = rest.postForEntity(BASE_MAPPING, createRentRequest, CreateRentResponse.class);
    assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

    verify(rentService).createRent(createRentRequest);

    Rent responseRent = createResponse.getBody().getRent();
    assertNotNull(responseRent);
    assertEquals(item.getId(), responseRent.getItem().getId());
    assertEquals(userPassport.getId(), responseRent.getRenter().getId());
  }

  @Test
  void testDeleteRentEndpoint() {
    UUID uuid = UUID.randomUUID();
    rest.delete(BASE_MAPPING + "/{rentId}", Map.of("rentId", uuid));

    verify(rentService).deleteRent(uuid);
  }

  @Test
  void testUpdateRentEndpoint() {
    Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant end = Instant.now().plus(2, ChronoUnit.HOURS);
    Rent testRent = new Rent(start, end, userPassport, item);
    testRent = rentRepository.save(testRent);
    UUID uuid = testRent.getId();

    UpdateRentRequest updateRequest = new UpdateRentRequest(Instant.now().plus(3, ChronoUnit.HOURS),
        Instant.now().plus(4, ChronoUnit.HOURS));
    HttpEntity<UpdateRentRequest> requestHttpEntity = new HttpEntity<>(updateRequest);
    rest.exchange(BASE_MAPPING + "/{id}", HttpMethod.PATCH,
        requestHttpEntity, void.class, Map.of("id", uuid));

    verify(rentService).updateRent(uuid, updateRequest);
  }

  @Test
  void testPinPhotoConfirmationEndpoint() {
    UUID uuid = UUID.randomUUID();
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(new byte[] {1, 2, 3});
    rest.postForEntity(BASE_MAPPING + "/{rentId}/confirm",
        request, void.class,
        Map.of("rentId", uuid));

    verify(rentService).confirmRentFinish(eq(uuid), any());
  }

  @Test
  void testGetPhotoConfirmationEndpoint() {
    UUID uuid = UUID.randomUUID();
    rest.getForEntity(BASE_MAPPING + "/{rentId}/confirm",
        byte[].class,
        Map.of("rentId", uuid));

    verify(rentService).getPhotoForRent(uuid);
  }

  @Test
  void testStartRentEndpoint() {
    UUID uuid = UUID.randomUUID();
    var response = rest.postForEntity(BASE_MAPPING + "/{rentId}/begin",
        null, ClientServerError.class,
        Map.of("rentId", uuid));

    verify(rentService).startRent(uuid);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testFailStartRentEndpoint() {
    doThrow(VerificationFailedException.class).when(rentService).startRent(any());

    UUID uuid = UUID.randomUUID();
    var response = rest.postForEntity(BASE_MAPPING + "/{rentId}/begin",
        null, ClientServerError.class,
        Map.of("rentId", uuid));

    verify(rentService).startRent(uuid);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void testEndRentEndpoint() {
    UUID uuid = UUID.randomUUID();
    var response = rest.postForEntity(BASE_MAPPING + "/{rentId}/end",
        null, ClientServerError.class,
        Map.of("rentId", uuid));

    verify(rentService).endRent(uuid);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  void testFailEndRentEndpoint() {
    doThrow(VerificationFailedException.class).when(rentService).endRent(any());

    UUID uuid = UUID.randomUUID();
    var response = rest.postForEntity(BASE_MAPPING + "/{rentId}/end",
        null, ClientServerError.class,
        Map.of("rentId", uuid));

    verify(rentService).endRent(uuid);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }
}
