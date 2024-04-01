package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetItemWithRentsResponse;
import com.mipt.hsse.hssetechbackend.rent.controllers.responses.GetRentResponse;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting/item")
public class ItemController {
  private final ItemService itemService;

  // TODO: Lock service is not implemented yet
  // private final LockService lockService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping()
  public ResponseEntity<Item> createItem(@Valid @RequestBody CreateItemRequest request) {
    try {
    Item createdItem = itemService.createItem(request);
    return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    } catch (EntityNotFoundException e) {
      throw new EntityNotFoundException("Expected item type does not exist");
    }
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateItem(
      @PathVariable("id") UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
    if (itemService.existsById(itemId)) {
      itemService.updateItem(itemId, request);
      return ResponseEntity.noContent().build();
    } else {
      throw new EntityNotFoundException();
    }
  }

  @GetMapping("/{id}")
  public GetItemResponse getItem(
      @PathVariable("id") UUID itemId,
      @RequestParam(value = "loadRentInfo", defaultValue = "false") boolean loadRentInfo) {
    Optional<Item> itemOpt = itemService.getItem(itemId);
    Item item = itemOpt.orElseThrow(EntityNotFoundException::new);

    GetItemResponse response;
    if (loadRentInfo) {
      Set<Rent> rents = itemService.getRentsOfItem(itemId);
      Set<GetRentResponse> rentsResponses = rents.stream().map(GetRentResponse::getFromRent).collect(Collectors.toSet());
      response = new GetItemWithRentsResponse(item, rentsResponses);
    } else {
      response = new GetItemResponse(item);
    }

    return response;
  }

  @GetMapping("/{item_id}/qr")
  public void getItemBookingQRCode(@PathVariable("item_id") UUID itemId) {
    throw new UnsupportedOperationException();
  }

  @PostMapping("/{item_id}/open")
  public void provideAccessToItem(@PathVariable("item_id") UUID itemId) {
    if (!itemService.existsById(itemId)) throw new EntityNotFoundException();

    UUID lockId = itemService.getItemLockId(itemId);

    // TODO: Lock service is not implemented yet
    throw new UnsupportedOperationException("Lock service is not implemented yet");
    //    if (!lockService.existsById(lockId))
    //      throw new EntityNotFoundException("The lock that is assigned to this item does not
    // exist");
    //
    //    lockService.requireOpenById(lockId);
  }

  @DeleteMapping("/{itemId}")
  public void deleteItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
