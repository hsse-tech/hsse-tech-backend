package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final JpaItemTypeRepository itemTypeRepository;
  private final JpaItemRepository itemRepository;


  public ItemService(JpaItemTypeRepository itemTypeRepository, JpaItemRepository itemRepository) {
    this.itemTypeRepository = itemTypeRepository;
    this.itemRepository = itemRepository;
  }

  @Transactional
  public ItemType createItemType(CreateItemTypeRequest request) {
    throw new UnsupportedOperationException();
  }

  @Transactional
  public Item createItem(CreateItemRequest request) {
    throw new UnsupportedOperationException();
  }
}
