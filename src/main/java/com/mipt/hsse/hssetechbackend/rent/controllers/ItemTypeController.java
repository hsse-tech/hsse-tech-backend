package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ItemType> createItemType(
      @Valid @RequestBody CreateItemTypeRequest request) {
    ItemType itemType = itemTypeService.createItemType(request);
    return new ResponseEntity<>(itemType, HttpStatus.CREATED);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> updateItemType(
      @PathVariable("id") UUID itemTypeId, @Valid @RequestBody UpdateItemTypeRequest request) {
    itemTypeService.updateItemType(itemTypeId, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.OK)
  public ResponseEntity<Void> deleteItemType(@PathVariable("id") UUID itemTypeId) {
    itemTypeService.deleteItemType(itemTypeId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ItemType> getItemType(@PathVariable("id") UUID itemTypeId) {
    Optional<ItemType> itemType = itemTypeService.getItemType(itemTypeId);

    if (itemType.isPresent()) return new ResponseEntity<>(itemType.get(), HttpStatus.OK);
    else throw EntityNotFoundException.itemTypeNotFound(itemTypeId);
  }

  @GetMapping
  public List<ItemType> getAllItemTypes() {
    return itemTypeService.getAllItemTypes();
  }
}
