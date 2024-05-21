package com.mipt.hsse.hssetechbackend.rent.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaHumanUserPassportRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRoleRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateRentRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.CreateRentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.mipt.hsse.hssetechbackend.users.controllers.SecurityConfiguration;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(RentController.class)
@Import({SecurityConfiguration.class,ObjectMapper.class})
class RentControllerTest {
  @MockBean
  private JpaHumanUserPassportRepository jpaHumanUserPassportRepository;
  @MockBean
  private JpaRoleRepository jpaRoleRepository;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void setup() {
    this.mockMvc =
            MockMvcBuilders
                    .webAppContextSetup(this.webApplicationContext)
                    .apply(springSecurity())
                    .build();
  }
  private static final String BASE_MAPPING = "/api/renting/rent";
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 120, false);
  private final Item item = new Item("Item name", itemType);
  private final User user = new User("human");
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Name", "Surname", "email@gmail.com", user);
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @MockBean private RentService rentService;

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
    Rent responseRent = objectMapper.readValue(mvcResult, Rent.class);

    verify(rentService).createRent(createRentRequest);

    assertNotNull(responseRent);
  }

  @Test
  void testBadRequestOnCreateRentFailed() throws Exception {
    final String errorText = "Error text";
    when(rentService.createRent(any())).thenThrow(new CreateRentProcessingException(errorText));

    CreateRentRequest createRentRequest =
        new CreateRentRequest(UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now());
    String requestStr = objectMapper.writeValueAsString(createRentRequest);

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING).content(requestStr).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  void getRentEndpoint() throws Exception {
    Rent rent = new Rent(Instant.now(), Instant.now(), userPassport, item);
    when(rentService.findById(any())).thenReturn(rent);

    var mvcResult =
        mockMvc
            .perform(get(BASE_MAPPING + "/{rent_id}", UUID.randomUUID()))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
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
    String errorText = "Error text";
    doThrow(new VerificationFailedException(errorText)).when(rentService).updateRent(any(), any());

    UUID uuid = UUID.randomUUID();
    UpdateRentRequest updateRequest = new UpdateRentRequest(Instant.now(), Instant.now());
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    var mvcResult =
        mockMvc
            .perform(
                patch(BASE_MAPPING + "/{id}", uuid)
                    .content(requestStr)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  void testPinPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {1, 2, 3, 4};

    mockMvc.perform(
        post(BASE_MAPPING + "/{rentId}/confirm", uuid)
            .content(photoBytes)
            .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE));

    verify(rentService).confirmRentFinish(eq(uuid), aryEq(photoBytes));
  }

  @Test
  void testGetPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    when(rentService.getPhotoForRent(any())).thenReturn(photoBytes);

    byte[] responseBytes =
        mockMvc
            .perform(get(BASE_MAPPING + "/{rentId}/confirm", uuid))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    verify(rentService).getPhotoForRent(uuid);
    assertArrayEquals(photoBytes, responseBytes);
  }

  @Test
  void testStartRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/begin", uuid)).andExpect(status().isOk());
    verify(rentService).startRent(uuid);
  }

  @Test
  void testFailStartRentEndpoint() throws Exception {
    String errorText = "Error text";
    doThrow(new VerificationFailedException(errorText)).when(rentService).startRent(any());

    UUID uuid = UUID.randomUUID();

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING + "/{rentId}/begin", uuid))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    verify(rentService).startRent(uuid);

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  void testEndRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/end", uuid)).andExpect(status().isOk());

    verify(rentService).endRent(uuid);
  }

  @Test
  void testFailEndRentEndpoint() throws Exception {
    String errorText = "Error text";
    doThrow(new VerificationFailedException(errorText)).when(rentService).endRent(any());

    UUID uuid = UUID.randomUUID();
    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING + "/{rentId}/end", uuid))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    verify(rentService).endRent(uuid);

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }
}
