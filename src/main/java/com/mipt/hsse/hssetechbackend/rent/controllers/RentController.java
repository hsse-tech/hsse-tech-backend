package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.*;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting/rent")
public class RentController {
  private final RentService rentService;

  @Autowired
  public RentController(RentService rentService) {
    this.rentService = rentService;
  }

  @PostMapping
  public ResponseEntity<Rent> createRent(
      @AuthenticationPrincipal OAuth2User user,
      @Valid @RequestBody CreateRentRequest createRequest) {
    UUID userId = OAuth2UserHelper.getUserId(user);
    Rent rent = rentService.createRent(userId, createRequest);
    return new ResponseEntity<>(rent, HttpStatus.CREATED);
  }

  @DeleteMapping("/{rent_id}")
  public void deleteRent(@PathVariable("rent_id") UUID rentId) {
    rentService.deleteRent(rentId);
  }

  @PatchMapping("/{rent_id}")
  @ResponseStatus(HttpStatus.OK)
  public void updateRent(
      @PathVariable("rent_id") UUID rentId, @Valid @RequestBody UpdateRentRequest request) {
      rentService.updateRent(rentId, request);
  }

  @PostMapping(value = "/{rent_id}/confirm", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public void pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId, HttpServletRequest photoServletRequest)
      throws IOException {
    byte[] photoBytes = photoServletRequest.getInputStream().readAllBytes();

    rentService.confirmRentFinish(rentId, photoBytes);
  }

  @GetMapping("/{rent_id}/confirm")
  public @ResponseBody Resource getPhotoConfirmation(@PathVariable("rent_id") UUID rentId) {
    byte[] photoBytes = rentService.getPhotoForRent(rentId);
    return new ByteArrayResource(photoBytes);
  }

  @GetMapping("/{rent_id}")
  public ResponseEntity<RentDTO> getRent(@PathVariable("rent_id") UUID rentId) {
    return new ResponseEntity<>(new RentDTO(rentService.findById(rentId)), HttpStatus.OK);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("{rent_id}/begin")
  public void startRent(@PathVariable("rent_id") UUID rentId) {
    rentService.startRent(rentId);
  }

  @PostMapping("{rent_id}/end")
  @ResponseStatus(HttpStatus.OK)
  public void endRent(@PathVariable("rent_id") UUID rentId) {
    rentService.endRent(rentId);
  }

  @ExceptionHandler(VerificationFailedException.class)
  protected ResponseEntity<ApiError> handleVerificationFailedException(
      VerificationFailedException ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }

  @ExceptionHandler({CreateRentProcessingException.class, DeleteRentProcessingException.class})
  protected ResponseEntity<ApiError> handleRentProcessingException(
      RentProcessingException ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
