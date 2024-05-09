package com.mipt.hsse.hssetechbackend.rent.services;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.mipt.hsse.hssetechbackend.data.entities.Item;
import com.mipt.hsse.hssetechbackend.data.entities.ItemType;
import com.mipt.hsse.hssetechbackend.data.entities.Rent;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaItemTypeRepository;
import com.mipt.hsse.hssetechbackend.data.repositories.JpaRentRepository;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import com.mipt.hsse.hssetechbackend.rent.qrcodegeneration.QrCodeManager;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
  private final JpaItemRepository itemRepository;
  private final JpaItemTypeRepository itemTypeRepository;
  private final JpaRentRepository rentRepository;

  public ItemService(
      JpaItemRepository itemRepository,
      JpaItemTypeRepository itemTypeRepository,
      JpaRentRepository rentRepository) {
    this.itemRepository = itemRepository;
    this.itemTypeRepository = itemTypeRepository;
    this.rentRepository = rentRepository;
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
    }
  }

  public Optional<Item> getItem(UUID uuid) {
    return itemRepository.findById(uuid);
  }

  public List<Rent> getFutureRentsOfItem(UUID itemId) {
    return rentRepository.findAllFutureRentsOfItem(
        getItem(itemId).orElseThrow(EntityNotFoundException::new));
  }

  public boolean existsById(UUID itemId) {
    return itemRepository.existsById(itemId);
  }

  public UUID getItemLockId(UUID itemId) {
    throw new UnsupportedOperationException();
  }

  public void provideAccessToItem(UUID itemId) {
    UUID lockId = getItemLockId(itemId);

    // TODO: Lock service is not implemented yet
    throw new UnsupportedOperationException("Lock service is not implemented yet");

    //    if (!lockService.existsById(lockId))
    //      throw new EntityNotFoundException("The lock that is assigned to this item does not
    // exist");
    //
    //    lockService.requireOpenById(lockId);
  }

  public byte[] getQrCodeForItem(UUID itemId, int width, int height) throws WriterException, IOException {
    // TODO: When we have domain, it should be put in here
    BitMatrix qrCodeMatrix =
        QrCodeManager.createQR("https://{DOMAIN}/rent/" + itemId, height, width);
    BufferedImage image = MatrixToImageWriter.toBufferedImage(qrCodeMatrix);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

  public List<Item> getAllItems() {
    return itemRepository.findAll();
  }
}
