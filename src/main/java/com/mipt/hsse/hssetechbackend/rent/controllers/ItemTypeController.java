package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.apierrorhandling.ApiError;
import com.mipt.hsse.hssetechbackend.apierrorhandling.RestExceptionHandler;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
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
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void updateItemType(
      @PathVariable("id") UUID itemTypeId, @Valid @RequestBody UpdateItemTypeRequest request) {
      itemTypeService.updateItemType(itemTypeId, request);
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

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ApiError> entityNotFoundExceptionHandler(
      EntityNotFoundException ex) {
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getMessage());
    return RestExceptionHandler.buildResponseEntity(apiError);
  }
}
