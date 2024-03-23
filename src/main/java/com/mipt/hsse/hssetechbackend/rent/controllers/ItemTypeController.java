package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.services.ItemTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/renting")
public class ItemTypeController {
  private final ItemTypeService itemTypeService;

  public ItemTypeController(ItemTypeService itemTypeService) {
    this.itemTypeService = itemTypeService;
  }

  @PostMapping("/create-item-type")
  @ResponseStatus(HttpStatus.CREATED)
  public ItemType createItemType(@RequestBody CreateItemTypeRequest request) {
    return itemTypeService.createItemType(request);
  }

  @DeleteMapping("/delete-item-type/{itemTypeId}")
  @ResponseStatus(HttpStatus.OK)
  public void createItemType(@PathVariable("itemTypeId") UUID itemTypeId) {
    itemTypeService.deleteItemType(itemTypeId);
  }

  @GetMapping("/item-type/{itemTypeId}")
  public Optional<ItemType> getItemType(@PathVariable("itemTypeId") UUID itemTypeId) {
    return itemTypeService.getItemType(itemTypeId);
  }
}
