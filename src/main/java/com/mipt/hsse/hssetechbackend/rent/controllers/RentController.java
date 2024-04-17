package com.mipt.hsse.hssetechbackend.rent.controllers;

import ch.qos.logback.core.net.server.Client;
import com.mipt.hsse.hssetechbackend.auxiliary.serializablebytesarray.BytesArray;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.CreateRentResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.RentProcessingException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.VerificationFailedException;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import com.sun.source.tree.NewClassTree;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.UUID;

import jdk.jshell.Snippet;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<CreateRentResponse> createRent(
      @Valid @RequestBody CreateRentRequest request) {
    try {
      Rent rent = rentService.createRent(request);
      CreateRentResponse response = CreateRentResponse.respondSuccess(rent);
      return new ResponseEntity<>(response, HttpStatus.CREATED);
    } catch (RentProcessingException e) {
      return new ResponseEntity<>(
          CreateRentResponse.respondFailed(e.toString()), HttpStatus.BAD_REQUEST);
    }
  }

  @DeleteMapping("/{rent_id}")
  public void deleteRent(@PathVariable("rent_id") UUID rentId) {
    rentService.deleteRent(rentId);
  }

  @PatchMapping("/{rent_id}")
  public ResponseEntity<ClientServerError> updateRent(@PathVariable("rent_id") UUID rentId,
                                                      @Valid @RequestBody UpdateRentRequest request) {
    try {
      rentService.updateRent(rentId, request);
      return ResponseEntity.ok(null);
    } catch (VerificationFailedException e) {
      return ResponseEntity.badRequest().body(new ClientServerError(e.toString()));
    }
  }

  @PostMapping("/{rent_id}/confirm")
  public void pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId,
      @Valid @RequestBody PinPhotoConfirmationRequest request) {
    rentService.confirmRentFinish(rentId, request);
  }

  @GetMapping("/{rent_id}/confirm")
  public ResponseEntity<BytesArray> getPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId) {
    return new ResponseEntity<>(rentService.getPhotoForRent(rentId), HttpStatus.OK);
  }

  @GetMapping("/{rent_id}")
  public ResponseEntity<RentDTO> getRent(@PathVariable("rent_id") UUID rentId) {
    return new ResponseEntity<>(new RentDTO(rentService.findById(rentId)), HttpStatus.OK);
  }

  @PostMapping("{rent_id}/begin")
  public ResponseEntity<ClientServerError> startRent(@PathVariable("rent_id") UUID rentId) {
    try {
      rentService.startRent(rentId);
      return ResponseEntity.ok(null);
    } catch (VerificationFailedException e) {
      return ResponseEntity.badRequest().body(new ClientServerError(e.getMessage()));
    }
  }

  @PostMapping("{rent_id}/end")
  public ResponseEntity<ClientServerError> endRent(@PathVariable("rent_id") UUID rentId) {
    try {
      rentService.endRent(rentId);
      return ResponseEntity.ok(null);
    } catch (VerificationFailedException e) {
      return ResponseEntity.badRequest().body(new ClientServerError(e.getMessage()));
    }
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
