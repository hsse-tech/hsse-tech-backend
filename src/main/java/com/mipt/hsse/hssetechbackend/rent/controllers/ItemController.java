package com.mipt.hsse.hssetechbackend.rent.controllers;

import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.rent.services.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/renting")
public class ItemController {
  private final ItemService itemService;

  public ItemController(ItemService itemService) {
    this.itemService = itemService;
  }

  @PostMapping("/create-item-type")
  public ItemType createItemType(@RequestBody CreateItemTypeRequest request) {
    return itemService.createItemType(request);
  }

  @PostMapping("/create-item")
  public Item createItem(@RequestBody CreateItemRequest request) {
    return itemService.createItem(request);
  }
}
