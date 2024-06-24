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
import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.oauth.config.SecurityConfig;
import com.mipt.hsse.hssetechbackend.oauth.services.MiptOAuth2UserService;
import com.mipt.hsse.hssetechbackend.oauth.services.UserPassportServiceBase;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RentController.class)
@Import({ObjectMapper.class, SecurityConfig.class, MiptOAuth2UserService.class})
class RentControllerTest {
  private static final String BASE_MAPPING = "/api/renting/rent";
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 120, false);
  private final Item item = new Item("Item name", itemType);
  private final User user = new User("human");
  private final HumanUserPassport userPassport =
      new HumanUserPassport(123L, "Name", "Surname", "email@gmail.com", user);

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private RentService rentService;

  @MockBean
  private UserPassportServiceBase passportService;

  @BeforeEach
  void setup() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  @Test
  @WithMockUser
  void testCreateRentEndpoint() throws Exception {
    Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
    Instant end = Instant.now().plus(2, ChronoUnit.HOURS);

    when(rentService.createRent(any())).thenReturn(new Rent(start, end, userPassport, item));

    CreateRentRequest createRentRequest =
        new CreateRentRequest(UUID.randomUUID(), UUID.randomUUID(), start, end);
    String requestStr = objectMapper.writeValueAsString(createRentRequest);

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING)
                      .content(requestStr)
                      .contentType(MediaType.APPLICATION_JSON)
                      .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
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
  @WithMockUser
  void testBadRequestOnCreateRentFailed() throws Exception {
    final String errorText = "Error text";
    when(rentService.createRent(any())).thenThrow(new CreateRentProcessingException(errorText));

    CreateRentRequest createRentRequest =
        new CreateRentRequest(UUID.randomUUID(), UUID.randomUUID(), Instant.now(), Instant.now());
    String requestStr = objectMapper.writeValueAsString(createRentRequest);

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING)
                      .content(requestStr)
                      .contentType(MediaType.APPLICATION_JSON)
                      .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  @WithMockUser
  void getRentEndpoint() throws Exception {
    Rent rent = new Rent(Instant.now(), Instant.now(), userPassport, item);
    when(rentService.findById(any())).thenReturn(rent);

    var mvcResult =
        mockMvc
            .perform(get(BASE_MAPPING + "/{rent_id}", UUID.randomUUID())
                      .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
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
  @WithMockUser
  void testDeleteRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(delete(BASE_MAPPING + "/{rent_id}", uuid)
              .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))));

    verify(rentService).deleteRent(uuid);
  }

  @Test
  @WithMockUser
  void testUpdateRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();

    UpdateRentRequest updateRequest =
        new UpdateRentRequest(
            Instant.now().plus(3, ChronoUnit.HOURS), Instant.now().plus(4, ChronoUnit.HOURS));
    String requestStr = objectMapper.writeValueAsString(updateRequest);

    mockMvc.perform(
        patch(BASE_MAPPING + "/{id}", uuid)
            .content(requestStr)
            .contentType(MediaType.APPLICATION_JSON)
            .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))));

    verify(rentService).updateRent(uuid, updateRequest);
  }

  @Test
  @WithMockUser
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
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  @WithMockUser
  void testPinPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {1, 2, 3, 4};

    mockMvc.perform(
        post(BASE_MAPPING + "/{rentId}/confirm", uuid)
            .content(photoBytes)
            .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))));

    verify(rentService).confirmRentFinish(eq(uuid), aryEq(photoBytes));
  }

  @Test
  @WithMockUser
  void testGetPhotoConfirmationEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    byte[] photoBytes = new byte[] {0, 1, 2, 3};
    when(rentService.getPhotoForRent(any())).thenReturn(photoBytes);

    byte[] responseBytes =
        mockMvc
            .perform(get(BASE_MAPPING + "/{rentId}/confirm", uuid)
                      .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andReturn()
            .getResponse()
            .getContentAsByteArray();

    verify(rentService).getPhotoForRent(uuid);
    assertArrayEquals(photoBytes, responseBytes);
  }

  @Test
  @WithMockUser
  void testStartRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/begin", uuid)
            .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andExpect(status().isOk());
    verify(rentService).startRent(uuid);
  }

  @Test
  @WithMockUser
  void testFailStartRentEndpoint() throws Exception {
    String errorText = "Error text";
    doThrow(new VerificationFailedException(errorText)).when(rentService).startRent(any());

    UUID uuid = UUID.randomUUID();

    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING + "/{rentId}/begin", uuid)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    verify(rentService).startRent(uuid);

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }

  @Test
  @WithMockUser
  void testEndRentEndpoint() throws Exception {
    UUID uuid = UUID.randomUUID();
    mockMvc.perform(post(BASE_MAPPING + "/{rentId}/end", uuid)
            .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
        .andExpect(status().isOk());

    verify(rentService).endRent(uuid);
  }

  @Test
  @WithMockUser
  void testFailEndRentEndpoint() throws Exception {
    String errorText = "Error text";
    doThrow(new VerificationFailedException(errorText)).when(rentService).endRent(any());

    UUID uuid = UUID.randomUUID();
    var mvcResult =
        mockMvc
            .perform(post(BASE_MAPPING + "/{rentId}/end", uuid)
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_MIPT_USER"))))
            .andExpect(status().isBadRequest())
            .andReturn()
            .getResponse()
            .getContentAsString();

    verify(rentService).endRent(uuid);

    ApiError exception = objectMapper.readValue(mvcResult, ApiError.class);
    assertEquals(errorText, exception.getMessage());
  }
}
