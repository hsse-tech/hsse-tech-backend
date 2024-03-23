package com.mipt.hsse.hssetechbackend.rent.controllers.requests;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;

public record CreateItemRequest(String displayName, ItemType itemType) {
  public Item getItem() {
    return new Item(displayName, itemType);
  }
}
