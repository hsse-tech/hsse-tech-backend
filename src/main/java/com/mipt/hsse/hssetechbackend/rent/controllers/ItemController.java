package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
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

  @DeleteMapping("/delete-item/{itemId}")
  @ResponseStatus(HttpStatus.OK)
  public void createItem(@PathVariable("itemId") UUID itemId) {
    itemService.deleteItem(itemId);
  }
}
