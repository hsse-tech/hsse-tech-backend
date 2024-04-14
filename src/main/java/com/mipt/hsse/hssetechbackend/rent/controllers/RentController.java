package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentDTO;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import jakarta.validation.Valid;
import java.io.ByteArrayInputStream;
import java.util.UUID;
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
  public ResponseEntity<Rent> createRent(@Valid @RequestBody CreateRentRequest request) {
    Rent rent = rentService.createRent(request);
    return new ResponseEntity<>(rent, HttpStatus.CREATED);
  }

  @DeleteMapping("/{rent_id}")
  public void deleteRent(@PathVariable("rent_id") UUID rentId) {
    rentService.deleteRent(rentId);
  }

  @PostMapping("/{rent_id}/confirm")
  public void pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId,
      @Valid @RequestBody PinPhotoConfirmationRequest request) {
      rentService.confirmRentFinish(rentId, request);
  }

  @GetMapping("/{rent_id}/confirm")
  public ResponseEntity<ByteArrayInputStream> getPhotoConfirmation(@PathVariable("rent_id") UUID rentId) {
      return new ResponseEntity<>(rentService.getPhotoForRent(rentId), HttpStatus.OK);
  }

  @GetMapping("/{rent_id}")
  public RentDTO getRent(@PathVariable("rent_id") UUID rentId) {
    return new RentDTO(rentService.findById(rentId));
  }

  @PostMapping("{rent_id}/begin")
  public void startRent(@PathVariable("rent_id") UUID rentId) {
       rentService.startRent(rentId);
  }

  @PostMapping("{rent_id}/end")
  public void endRent(@PathVariable("rent_id") UUID rentId) {
    rentService.endRent(rentId);
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
