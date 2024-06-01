package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemTypeRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ItemTypeService {
  private final JpaItemTypeRepository itemTypeRepository;

  public ItemTypeService(JpaItemTypeRepository itemTypeRepository) {
    this.itemTypeRepository = itemTypeRepository;
  }

  @Transactional
  public ItemType createItemType(CreateItemTypeRequest request) {
    ItemType itemType =
        new ItemType(
            request.cost(),
            request.displayName(),
            request.maxRentTimeMinutes(),
            request.isPhotoConfirmationRequired());

    return itemTypeRepository.save(itemType);
  }

  @Transactional
  public Optional<ItemType> getItemType(UUID itemTypeId) {
    return itemTypeRepository.findById(itemTypeId);
  }

  @Transactional
  public void updateItemType(UUID itemTypeId, UpdateItemTypeRequest request) {
    ItemType itemType =
        itemTypeRepository
            .findById(itemTypeId)
            .orElseThrow(() -> new EntityNotFoundException(ItemType.class, itemTypeId));

    if (request.newCost() != null) {
      itemType.setCost(request.newCost());
    }
    if (request.newDisplayName() != null) {
      itemType.setDisplayName(request.newDisplayName());
    }
    if (request.isPhotoConfirmationRequired() != null) {
      itemType.setPhotoRequiredOnFinish(request.isPhotoConfirmationRequired());
    }
    if (request.newMaxRentTimeMinutes() != null) {
      itemType.setMaxRentTimeMinutes(request.newMaxRentTimeMinutes());
    }

    itemTypeRepository.save(itemType);
  }

  public void deleteItemType(UUID itemTypeId) {
    if (itemTypeRepository.existsById(itemTypeId)) {
      itemTypeRepository.deleteById(itemTypeId);
    }
  }

  public List<ItemType> getAllItemTypes() {
    return itemTypeRepository.findAll();
  }
}
