package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.data.DTOs.RentingDTOs.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final JpaItemTypeRepository itemTypeRepository;
  private final JpaItemRepository itemRepository;

  public ItemService(JpaItemTypeRepository itemTypeRepository, JpaItemRepository itemRepository) {
    this.itemTypeRepository = itemTypeRepository;
    this.itemRepository = itemRepository;
  }

  public ItemType createItemType(CreateItemTypeRequest request) {
    throw new UnsupportedOperationException();
  }

  public Item createItem(CreateItemRequest request) {
    throw new UnsupportedOperationException();
  }
}
