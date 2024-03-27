package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.ClientServerError;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/renting/item-type")
public class ItemTypeController {
  private final ItemTypeService itemTypeService;

  public ItemTypeController(ItemTypeService itemTypeService) {
    this.itemTypeService = itemTypeService;
  }

  @PostMapping
  public ResponseEntity<ItemType> createItemType(
      @Valid @RequestBody CreateItemTypeRequest request) {
    ItemType itemType = itemTypeService.createItemType(request);
    return new ResponseEntity<>(itemType, HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<Void> updateItemType(
      @PathVariable("id") UUID itemTypeId, @Valid @RequestBody UpdateItemTypeRequest request) {
    if (itemTypeService.getItemType(itemTypeId).isPresent()) {
      itemTypeService.updateItemType(itemTypeId, request);
      return ResponseEntity.noContent().build();
    } else {
      throw new EntityNotFoundException();
    }
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public void deleteItemType(@PathVariable("id") UUID itemTypeId) {
    itemTypeService.deleteItemType(itemTypeId);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemType> getItemType(@PathVariable("id") UUID itemTypeId) {
    Optional<ItemType> itemType = itemTypeService.getItemType(itemTypeId);

    if (itemType.isPresent()) return new ResponseEntity<>(itemType.get(), HttpStatus.OK);
    else throw new EntityNotFoundException();
  }

  @ExceptionHandler
  public ResponseEntity<ClientServerError> entityNotFoundExceptionHandler(
      EntityNotFoundException e) {
    return new ResponseEntity<>(new ClientServerError(e.getMessage()), HttpStatus.BAD_REQUEST);
  }
}
