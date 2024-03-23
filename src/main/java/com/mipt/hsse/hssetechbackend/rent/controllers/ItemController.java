package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/renting")
public class ItemController {
  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping("/create-item-type")
  @ResponseStatus(HttpStatus.CREATED)
  public ItemType createItemType(@RequestBody CreateItemTypeRequest request) {
    return itemService.createItemType(request);
  }

  @DeleteMapping("/delete-item-type/{itemTypeId}")
  @ResponseStatus(HttpStatus.OK)
  public void createItemType(@PathVariable("itemTypeId") UUID itemTypeId) {
    itemService.deleteItemType(itemTypeId);
  }

  @GetMapping("/item-type/{itemTypeId}")
  public Optional<ItemType> getItemType(@PathVariable("itemTypeId") UUID itemTypeId) {
    return itemService.getItemType(itemTypeId);
  }

  @PostMapping("/create-item")
  @ResponseStatus(HttpStatus.CREATED)
  public Item createItem(@RequestBody CreateItemRequest request) {
    return itemService.createItem(request);
  }

  @DeleteMapping("/delete-item/{itemId}")
  @ResponseStatus(HttpStatus.OK)
  public void createItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }
}
