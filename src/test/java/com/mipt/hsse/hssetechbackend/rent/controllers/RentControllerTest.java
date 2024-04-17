package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray.BytesArray;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.PinPhotoConfirmationRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.CreateRentResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RentController.class)
@Import(ObjectMapper.class)
class RentControllerTest extends DatabaseSuite {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @MockBean private RentService rentService;

  private static final String BASE_MAPPING = "/api/renting/rent";

  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 120, false);
  private final Item item = new Item("Item name", itemType);
  private final User user = new User("human");
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Name", "Surname", "email@gmail.com", user);

  @BeforeEach
  void setupObjectMapper() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  void testCreateRentEndpoint() throws Exception {
    Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

    when(rentService.createRent(any())).thenReturn(new Rent(start, end, userPassport, item));

    CreateRentRequest createRentRequest =
        new CreateRentRequest(UUID.randomUUID(), UUID.randomUUID(), start, end);
    String requestStr = objectMapper.writeValueAsString(createRentRequest);

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING).content(requestStr).contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    CreateRentResponse createRentResponse =
        objectMapper.readValue(mvcResult, CreateRentResponse.class);

    verify(rentService).createRent(createRentRequest);

    Rent responseRent = createRentResponse.getRent();
    assertNotNull(responseRent);
  }

  @Test
  void testBadRequestOnCreateRentFailed() throws Exception {
    when(rentService.createRent(any())).thenThrow(new RentProcessingException());

    CreateRentRequest createRentRequest =
        new CreateRentRequest(UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now());
    String requestStr = objectMapper.writeValueAsString(createRentRequest);

    mockMvc
        .perform(post(BASE_MAPPING).content(requestStr).contentType(MediaType.APPLICATION_JSON))

        .andExpect(status().isBadRequest());
  }

  @Test
  void getRentEndpoint() throws Exception {
    Rent rent = new Rent(Instant.now(), Instant.now(), userPassport, item);
    when(rentService.findById(any())).thenReturn(rent);

    var mvcResult= mockMvc.perform(get(BASE_MAPPING + "/{rent_id}", UUID.randomUUID())).andDo(print()).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    RentDTO retrievedRentDTO = objectMapper.readValue(mvcResult, RentDTO.class);

    assertEquals(rent.getFactStart(), retrievedRentDTO.factStart());
    assertEquals(rent.getFactEnd(), retrievedRentDTO.factEnd());
    assertEquals(rent.getItem().getDisplayName(), retrievedRentDTO.item().getDisplayName());
  }

  @Test
  void testDeleteRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(delete(BASE_MAPPING + "/{rent_id}", uuid));

    verify(rentService).deleteRent(uuid);
  }

  @Test
  void testUpdateRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();

    UpdateRentRequest updateRequest =
        new UpdateRentRequest(
            Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS));
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc.perform(
        patch(BASE_MAPPING + "/{id}", uuid)
            .content(requestStr)
            .contentType(MediaType.APPLICATION_JSON));

    verify(rentService).updateRent(uuid, updateRequest);
  }

  @Test
  void testBadRequestOnUpdateFailed() throws Exception {
    doThrow(VerificationFailedException.class).when(rentService).updateRent(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateRentRequest updateRequest =
        new UpdateRentRequest(
            Instant.now(), Instant.now());
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc.perform(
        patch(BASE_MAPPING + "/{id}", uuid)
            .content(requestStr)
            .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
  }

  @Test
  void testPinPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    BytesArray bytesArray = new BytesArray(new byte[] {1, 2, 3, 4});
    PinPhotoConfirmationRequest request = new PinPhotoConfirmationRequest(bytesArray);
    String requestStr = objectMapper.writeValueAsString(request);

    mockMvc.perform(
        post(BASE_MAPPING + "/{rentId}/confirm", uuid)
            .content(requestStr)
            .contentType(MediaType.APPLICATION_JSON));

    verify(rentService).confirmRentFinish(eq(uuid), eq(request));
  }

  @Test
  void testGetPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    BytesArray bytesArray = new BytesArray(new byte[] {0, 1, 2, 3});
    when(rentService.getPhotoForRent(any())).thenReturn(bytesArray);

    var mvcResult =
        mockMvc
            .perform(get(BASE_MAPPING + "/{rentId}/confirm", uuid))
            .andReturn()
            .getResponse()
            .getContentAsString();
    BytesArray responseBytesArray = objectMapper.readValue(mvcResult, BytesArray.class);

    verify(rentService).getPhotoForRent(uuid);
    assertEquals(bytesArray, responseBytesArray);
  }

  @Test
  void testStartRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/begin", uuid)).andExpect(status().isOk());
    verify(rentService).startRent(uuid);
  }

  @Test
  void testFailStartRentEndpoint() throws Exception {
    doThrow(VerificationFailedException.class).when(rentService).startRent(any());

    UUID uuid = UUID.randomUUID();
    mockMvc
        .perform(post(BASE_MAPPING + "/{rentId}/begin", uuid))
        .andExpect(status().isBadRequest());

    verify(rentService).startRent(uuid);
  }

  @Test
  void testEndRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/end", uuid)).andExpect(status().isOk());

    verify(rentService).endRent(uuid);
  }

  @Test
  void testFailEndRentEndpoint() throws Exception {
    doThrow(VerificationFailedException.class).when(rentService).endRent(any());

    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/end", uuid)).andExpect(status().isBadRequest());

    verify(rentService).endRent(uuid);
  }
}
