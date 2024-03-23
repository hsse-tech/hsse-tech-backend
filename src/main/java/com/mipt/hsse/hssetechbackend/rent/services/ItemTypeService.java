package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ItemTypeService {
  private final JpaItemTypeRepository itemTypeRepository;

  public ItemTypeService(JpaItemTypeRepository itemTypeRepository) {
    this.itemTypeRepository = itemTypeRepository;
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
  public Optional<ItemType> getItemType(UUID itemTypeId) {
    return itemTypeRepository.findById(itemTypeId);
  }
}
