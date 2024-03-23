package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final JpaItemRepository itemRepository;

  public ItemService(JpaItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Transactional
  public Item createItem(CreateItemRequest request) {
    Item item = request.getItem();

    return itemRepository.save(item);
  }

  @Transactional
  public void updateItem(UUID itemId, UpdateItemRequest request) {
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));

    if (request.newDisplayName() != null) item.setDisplayName(request.newDisplayName());

    itemRepository.save(item);
  }

  @Transactional
  public void deleteItem(UUID itemId) {
    if (itemRepository.findById(itemId).isPresent()) itemRepository.deleteById(itemId);
    else throw new EntityNotFoundException(Item.class, itemId);
  }
}
