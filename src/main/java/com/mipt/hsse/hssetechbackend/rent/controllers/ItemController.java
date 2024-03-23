package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import jakarta.persistence.EntityNotFoundException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting")
public class ItemController {
  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping("/create-item")
  @ResponseStatus(HttpStatus.CREATED)
  public Item createItem(@RequestBody CreateItemRequest request) {
    return itemService.createItem(request);
  }

  @PatchMapping("/items/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void updateItem(@PathVariable("id") UUID itemId, @RequestBody UpdateItemRequest request) {
    itemService.updateItem(itemId, request);
  }

  @GetMapping("/items/{item_id}/qr")
  public void getItemBookingQRCode(@PathVariable("item_id") UUID itemId) {
    throw new UnsupportedOperationException();
  }

  @DeleteMapping("/delete-item/{itemId}")
  @ResponseStatus(HttpStatus.OK)
  public void createItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.NOT_FOUND);
  }
}
