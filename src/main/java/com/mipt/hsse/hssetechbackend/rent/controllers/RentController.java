package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.oauth.services.OAuth2UserHelper;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.*;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import com.mipt.hsse.hssetechbackend.utils.PngUtility;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
  public ResponseEntity<Void> deleteRent(@PathVariable("rent_id") UUID rentId) {
    rentService.deleteRent(rentId);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{rent_id}")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Void> updateRent(
      @PathVariable("rent_id") UUID rentId, @Valid @RequestBody UpdateRentRequest request) {
    rentService.updateRent(rentId, request);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/{rent_id}/confirm", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<Void> pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId, HttpServletRequest photoServletRequest)
      throws IOException {
    byte[] photoBytes = photoServletRequest.getInputStream().readAllBytes();

    // Ensure png format
    if (!PngUtility.isPngFormat(photoBytes)) {
      return ResponseEntity.badRequest().build();
    }

    rentService.confirmRentFinish(rentId, photoBytes);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{rent_id}/confirm")
  public ResponseEntity<Resource> getPhotoConfirmation(@PathVariable("rent_id") UUID rentId) {
    byte[] photoBytes = rentService.getPhotoForRent(rentId);
    var returnResource = new ByteArrayResource(photoBytes);

    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"confirmation.png\"")
        .body(returnResource);
  }

  @GetMapping("/{rent_id}")
  public ResponseEntity<RentDTO> getRent(@PathVariable("rent_id") UUID rentId) {
    return new ResponseEntity<>(new RentDTO(rentService.findById(rentId)), HttpStatus.OK);
  }

  @ResponseStatus(HttpStatus.OK)
  @PostMapping("{rent_id}/begin")
  public ResponseEntity<Void> startRent(@PathVariable("rent_id") UUID rentId) {
    rentService.startRent(rentId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("{rent_id}/end")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Void> endRent(@PathVariable("rent_id") UUID rentId) {
    rentService.endRent(rentId);
    return ResponseEntity.ok().build();
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
