package com.mipt.hsse.hssetechbackend.rent.services;

import com.mipt.hsse.hssetechbackend.apierrorhandling.EntityNotFoundException;
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
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerErrorException;

@Service
public class ItemService {
  private final JpaItemRepository itemRepository;
  private final JpaItemTypeRepository itemTypeRepository;
  private final JpaRentRepository rentRepository;
  private final PhotoRepository photoRepository;
  private final LockServiceBase lockService;
  private final String appDomain;

  public ItemService(
      JpaItemRepository itemRepository,
      JpaItemTypeRepository itemTypeRepository,
      JpaRentRepository rentRepository,
      PhotoRepository photoRepository,
      LockServiceBase lockService,
      @Value("${app_domain}") String appDomain) {
    this.itemRepository = itemRepository;
    this.itemTypeRepository = itemTypeRepository;
    this.rentRepository = rentRepository;
    this.photoRepository = photoRepository;
    this.lockService = lockService;
    this.appDomain = appDomain;
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

  public void deleteItem(UUID itemId) throws IOException {
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

  //
  //  public byte[] getQrCodeForItem(UUID itemId, int width, int height)
  //      throws WriterException, IOException {
  //    BitMatrix qrCodeMatrix = QrCodeManager.createQR(buildLinkForItemQrCode(itemId), width,
  // height);
  //    BufferedImage image = MatrixToImageWriter.toBufferedImage(qrCodeMatrix);
  //
  //    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
  //    ImageIO.write(image, "png", byteArrayOutputStream);
  //    return byteArrayOutputStream.toByteArray();
  //  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }

  public void saveItemPhoto(UUID itemId, byte[] photoBytes) {
    if (!itemRepository.existsById(itemId)) {
      throw new EntityNotFoundException(Item.class, itemId);
    }

    try {
      photoRepository.save(PhotoType.ITEM_THUMBNAIL, itemId, photoBytes);
    } catch (IOException | NoSuchAlgorithmException | UnsupportedOperationException e) {
      throw new ServerErrorException("Unexpected IO error while saving photo", e);
    }
  }

  public byte[] getItemPhoto(UUID itemId) {
    if (!itemRepository.existsById(itemId)) {
      throw new EntityNotFoundException(Item.class, itemId);
    }

    try {
      return photoRepository.findPhoto(PhotoType.ITEM_THUMBNAIL, itemId);
    } catch (IOException e) {
      throw new ServerErrorException("Unexpected IO error while saving photo", e);
    }
  }

  //  private String buildLinkForItemQrCode(UUID itemId) {
  //
  //  }
}
