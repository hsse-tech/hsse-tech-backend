package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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

  @PostMapping()
  public Rent rentItem(@Valid @RequestBody RentItemRequest request) {
    return rentService.rentItem(request);
  }

  @DeleteMapping("/{rent_id}")
  public void unrentItem(@PathVariable("rent_id") UUID rentId) {
    rentService.unrentItem(rentId);
  }

  @PatchMapping("/edit-time")
  public void editRentTime(@Valid @RequestBody EditRentTimeRequest request) {
    throw new UnsupportedOperationException();
  }

  @PostMapping("/{rent_id}/confirm")
  public void pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId, @Valid @RequestBody PinPhotoConfirmationRequest request) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/confirm/photo/{id}")
  public void getPhotoConfirmation(@PathVariable("id") long photoId) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/{rent_id}/confirm")
  public Object getRentConfirmationPhoto(@PathVariable("rent_id") UUID rentId) {
    throw new UnsupportedOperationException();
  }

  /**
   * TODO: This query does not satisfy the brief in Figma, needs consideration, don't use it for the
   * time being
   */
  @GetMapping("/{rent_id}")
  public Rent getRent(@PathVariable("rent_id") UUID rentId) {
    return rentService.findById(rentId);
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
