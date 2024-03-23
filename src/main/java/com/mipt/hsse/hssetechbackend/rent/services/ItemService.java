package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
    ItemType itemType = request.getItemType();

    return itemTypeRepository.save(itemType);
  }

  @Transactional
  public void deleteItemType(UUID itemTypeId) {
    if (itemTypeRepository.findById(itemTypeId).isPresent())
      itemTypeRepository.deleteById(itemTypeId);
    else
      throw new EntityNotFoundException(ItemType.class, itemTypeId);
  }

  @Transactional
  public Item createItem(CreateItemRequest request) {
    Item item = request.getItem();

    return itemRepository.save(item);
  }

  @Transactional
  public void deleteItem(UUID itemId) {
    if (itemRepository.findById(itemId).isPresent())
      itemRepository.deleteById(itemId);
    else
      throw new EntityNotFoundException(Item.class, itemId);
  }
}
