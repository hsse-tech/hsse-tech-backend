package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.controllers.rent.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.entities.LockPassport;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository.PhotoType;
import com.mipt.hsse.hssetechbackend.lock.services.LockServiceBase;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final JpaItemRepository itemRepository;
  private final JpaItemTypeRepository itemTypeRepository;
  private final JpaRentRepository rentRepository;
  private final PhotoRepository photoRepository;
  private final LockServiceBase lockService;

  public ItemService(
      JpaItemRepository itemRepository,
      JpaItemTypeRepository itemTypeRepository,
      JpaRentRepository rentRepository,
      PhotoRepository photoRepository,
      LockServiceBase lockService) {
    this.itemRepository = itemRepository;
    this.itemTypeRepository = itemTypeRepository;
    this.rentRepository = rentRepository;
    this.photoRepository = photoRepository;
    this.lockService = lockService;
  }

  public Item createItem(CreateItemRequest request) {
    ItemType itemType =
        itemTypeRepository
            .findById(request.itemTypeId())
            .orElseThrow(() -> new EntityNotFoundException(ItemType.class, request.itemTypeId()));
    Item item = new Item(request.displayName(), itemType);

    return itemRepository.save(item);
  }

  public void updateItem(UUID itemId, UpdateItemRequest request) {
    Item item =
        itemRepository
            .findById(itemId)
            .orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));

    if (request.newDisplayName() != null) {
      item.setDisplayName(request.newDisplayName());
    }

    itemRepository.save(item);
  }

  public void deleteItem(UUID itemId) {
    if (itemRepository.existsById(itemId)) {
      itemRepository.deleteById(itemId);
      photoRepository.deletePhoto(PhotoType.ITEM_THUMBNAIL, itemId);
    }
  }

  public Optional<Item> getItem(UUID uuid) {
    return itemRepository.findById(uuid);
  }

  public List<Rent> getFutureRentsOfItem(UUID itemId) {
    return rentRepository.findAllFutureRentsOfItem(itemId);
  }

  public boolean existsById(UUID itemId) {
    return itemRepository.existsById(itemId);
  }

  public void provideAccessToItem(UUID itemId) {
    Item item = getItem(itemId).orElseThrow(() -> new EntityNotFoundException(Item.class, itemId));
    LockPassport lock = item.getLock();

    if (lock == null) throw new EntityNotFoundException(LockPassport.class, itemId);

    lockService.openLock(lock.getId());
  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public void saveItemPhoto(UUID itemId, byte[] photoBytes) {
    if (!itemRepository.existsById(itemId)) {
      throw new EntityNotFoundException(Item.class, itemId);
    }

      photoRepository.save(PhotoType.ITEM_THUMBNAIL, itemId, photoBytes);
  }

  public byte[] getItemPhoto(UUID itemId) {
    if (!itemRepository.existsById(itemId)) {
      throw new EntityNotFoundException(Item.class, itemId);
    }

      return photoRepository.findPhoto(PhotoType.ITEM_THUMBNAIL, itemId);
  }
}
