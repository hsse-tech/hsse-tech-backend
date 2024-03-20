package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.*;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.RentInfoResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.ShortRentInfo;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.services.RentService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting")
public class RentController {
  private final RentService rentService;

  @Autowired
  public RentController(RentService rentService) {
    this.rentService = rentService;
  }

  @PostMapping("/rent-item")
  public Rent rentItem(@RequestBody RentItemRequest request) {
    return rentService.rentItem(request);
  }

  @DeleteMapping("/unrent-item/{rent_id}")
  public void unrentItem(@PathVariable("rent_id") UUID rentId) {
    rentService.unrentItem(rentId);
  }

  @PatchMapping("/edit-time")
  public void editRentTime(@RequestBody EditRentTimeRequest request) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/items/{id}")
  public RentInfoResponse getRentInfo(
      @PathVariable("id") long id,
      @RequestParam(value = "loadRentInfo", defaultValue = "false") boolean loadRentInfo) {
    throw new UnsupportedOperationException();
  }

  @PostMapping("/{rent_id}/confirm/photo")
  public void pinPhotoConfirmation(
      @PathVariable("rent_id") UUID rentId, @RequestBody PinPhotoConfirmationRequest request) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/confirm/photo/{id}")
  public void getPhotoConfirmation(@PathVariable("id") long photoId) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/api/renting/{rent_id}")
  public Rent getRent(@PathVariable("rent_id") UUID rentId) {
    return rentService.findById(rentId);
  }

  @GetMapping("/items/{item_id}/qr")
  public void getItemBookingQRCode(@PathVariable("item_id") UUID itemId) {
    throw new UnsupportedOperationException();
  }

  @GetMapping("/{rent_id}")
  public ShortRentInfo getRentInfo(@PathVariable("rent_id") UUID rentId) {
    throw new UnsupportedOperationException();
  }

  @PatchMapping("/types/{id}")
  public void updateItemType(
      @PathVariable("id") UUID itemTypeId, @RequestBody UpdateItemTypeRequest request) {
    throw new UnsupportedOperationException();
  }

  @PatchMapping("/items/{id}")
  public void updateItem(@PathVariable UUID id, @RequestBody UpdateItemRequest request) {
    throw new UnsupportedOperationException();
  }

  @PostMapping("/items/{id}/open")
  public void requestOpenItem(@PathVariable("id") long itemId) {}

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.NOT_FOUND);
  }
}
