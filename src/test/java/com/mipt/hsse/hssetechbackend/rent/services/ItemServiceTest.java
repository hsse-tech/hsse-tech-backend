package com.mipt.hsse.hssetechbackend.rent.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoAlreadyExistsException;
import com.mipt.hsse.hssetechbackend.data.repositories.photorepository.PhotoRepository;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
import com.mipt.hsse.hssetechbackend.lock.services.LockService;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.CreateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.controllers.requests.UpdateItemRequest;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({ItemService.class, LockService.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemServiceTest extends DatabaseSuite {
  private final ItemType itemType = new ItemType(BigDecimal.ZERO, "Item type name", 60, false);
  @Autowired private ItemService itemService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaHumanUserPassportRepository humanUserPassportRepository;
  @Autowired private JpaRentRepository rentRepository;
  @MockBean private PhotoRepository photoRepository;
  @Autowired
  private LockService lockService;

  @BeforeEach
  void save() {
    itemTypeRepository.save(itemType);
  }

  @AfterEach
  public void clear() {
    itemTypeRepository.deleteAll();
    itemRepository.deleteAll();
    humanUserPassportRepository.deleteAll();
  }

  @Test
  void testCreateItem() {
    final String itemName = "Particular item name";
    var createItemRequest = new CreateItemRequest(itemName, itemType.getId());

    Item item = itemService.createItem(createItemRequest);

    Item extractedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertEquals(itemName, extractedItem.getDisplayName());
    assertEquals(itemType.getId(), extractedItem.getType().getId());
  }

  @Test
  void testSetItemThumbnailPhoto() throws Exception {
    byte[] photoBytes = new byte[]{1, 2, 3};
    Item item = new Item("test name", itemType);
    UUID uuid = itemRepository.save(item).getId();

    itemService.saveItemPhoto(uuid, photoBytes);

    verify(photoRepository).save(eq(PhotoRepository.PhotoType.ITEM_THUMBNAIL), eq(uuid), aryEq(photoBytes));
  }

  @Test
  void testSetItemPhotoThumbnailAlreadyExisting() throws Exception {
    byte[] photoBytes = new byte[]{1, 2, 3};
    Item item = new Item("test name", itemType);
    UUID uuid = itemRepository.save(item).getId();

    doThrow(PhotoAlreadyExistsException.class).when(photoRepository).save(any(), any(), any());

    assertThrows(PhotoAlreadyExistsException.class, () -> itemService.saveItemPhoto(uuid, photoBytes));
  }

  @Test
  void testGetItemThumbnailPhoto() throws Exception {
    byte[] photoBytes = new byte[]{1, 2, 3};

    when(photoRepository.findPhoto(any(), any())).thenReturn(photoBytes);

    Item item = new Item("test name", itemType);
    UUID uuid = itemRepository.save(item).getId();
    byte[] retrievedBytes = itemService.getItemPhoto(uuid);

    assertArrayEquals(photoBytes, retrievedBytes);
  }

  @Test
  void getFutureRents() {
    final String displayName = "Display name";

    HumanUserPassport humanUserPassport =
        new HumanUserPassport(123L, "firstName", "lastName", "test@gmail.com");
    humanUserPassport = humanUserPassportRepository.save(humanUserPassport);

    Item item = new Item(displayName, itemType);
    item = itemRepository.save(item);

    Item needlessItem = new Item("Dummy item", itemType);
    needlessItem = itemRepository.save(needlessItem);

    Rent rentBeforeNow =
        new Rent(
            Instant.now().minus(5, ChronoUnit.DAYS),
            Instant.now().minus(4, ChronoUnit.DAYS),
            humanUserPassport,
            item);
    rentRepository.save(rentBeforeNow);

    Rent rentBeginningNow =
        new Rent(Instant.now(), Instant.now().plus(1, ChronoUnit.DAYS), humanUserPassport, item);
    rentRepository.save(rentBeginningNow);

    Rent rentAfterNow =
        new Rent(
            Instant.now().plus(5, ChronoUnit.DAYS),
            Instant.now().plus(6, ChronoUnit.DAYS),
            humanUserPassport,
            item);
    rentRepository.save(rentAfterNow);

    Rent rentOfAnotherItem =
        new Rent(
            Instant.now().plus(7, ChronoUnit.DAYS),
            Instant.now().plus(8, ChronoUnit.DAYS),
            humanUserPassport,
            needlessItem);
    rentRepository.save(rentOfAnotherItem);

    List<Rent> futureRentsOfItem = itemService.getFutureRentsOfItem(item.getId());
    assertEquals(1, futureRentsOfItem.size());

    Rent retrievedRent = futureRentsOfItem.getFirst();
    assertEquals(rentAfterNow.getId(), retrievedRent.getId());

    rentRepository.deleteAll();
    itemRepository.deleteAll();
  }

  @Test
  void testFailCreateItemOfAbsentType() {
    final String itemName = "Particular item name";
    var createItemRequest = new CreateItemRequest(itemName, UUID.randomUUID());

    assertThrows(EntityNotFoundException.class, () -> itemService.createItem(createItemRequest));
  }

  @Test
  void testUpdateItem() {
    // Create item
    final String itemName = "Particular item name";
    var createItemRequest = new CreateItemRequest(itemName, itemType.getId());
    Item item = itemService.createItem(createItemRequest);

    // Update item
    final String newItemName = "New item name";
    var updateItemRequest = new UpdateItemRequest(newItemName);
    itemService.updateItem(item.getId(), updateItemRequest);

    Item extractedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertEquals(newItemName, extractedItem.getDisplayName());
    assertEquals(itemType.getId(), extractedItem.getType().getId());
  }

  @Test
  void testFailUpdateAbsentItem() {
    // Update item
    UpdateItemRequest updateItemRequest = new UpdateItemRequest("newDisplayName");
    assertThrows(
        EntityNotFoundException.class,
        () -> itemService.updateItem(UUID.randomUUID(), updateItemRequest));
  }

  @Test
  void testEmptyUpdateHasNoEffect() {
    // Create item
    final String itemName = "Particular item name";
    var createItemRequest = new CreateItemRequest(itemName, itemType.getId());
    Item item = itemService.createItem(createItemRequest);

    var updateItemRequest = new UpdateItemRequest(null);
    itemService.updateItem(item.getId(), updateItemRequest);

    Item extractedItem = itemRepository.findById(item.getId()).orElseThrow();
    assertEquals(itemName, extractedItem.getDisplayName());
  }

  @Test
  void testDeleteItem() throws IOException {
    final String itemName = "Particular item name";

    var createItemRequest = new CreateItemRequest(itemName, itemType.getId());
    Item item = itemService.createItem(createItemRequest);

    itemService.deleteItem(item.getId());

    assertTrue(itemRepository.findById(item.getId()).isEmpty());
  }

  @Test
  void testDeleteImageOnDeleteItem() throws IOException, NoSuchAlgorithmException {
    var createItemRequest = new CreateItemRequest("item name", itemType.getId());
    UUID uuid = itemService.createItem(createItemRequest).getId();

    // Pin photo
    byte[] photo = new byte[] {1, 2, 3};
    itemService.saveItemPhoto(uuid, photo);
    verify(photoRepository).save(eq(PhotoRepository.PhotoType.ITEM_THUMBNAIL), eq(uuid), aryEq(photo));

    // Delete item; expected to also delete photo
    itemService.deleteItem(uuid);
    verify(photoRepository).deletePhoto(eq(PhotoRepository.PhotoType.ITEM_THUMBNAIL), eq(uuid));
  }

  @Test
  void testProvideAccessToItem() throws ItemToLockCouplingException {
    var createItemRequest = new CreateItemRequest("item name", itemType.getId());
    UUID itemId = itemService.createItem(createItemRequest).getId();

    var lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), itemId);

    assertFalse(lockService.isLockOpen(lock.getId()));
    itemService.provideAccessToItem(itemId);
    assertTrue(lockService.isLockOpen(lock.getId()));
  }
}
