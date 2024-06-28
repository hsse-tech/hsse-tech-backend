package com.mipt.hsse.hssetechbackend.lock.services;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.lock.controllers.requests.CreateLockRequest;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemAlreadyHasLockException;
import com.mipt.hsse.hssetechbackend.rent.exceptions.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(LockService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LockServiceTest extends DatabaseSuite {
  @Autowired private LockService lockService;
  @Autowired private JpaItemTypeRepository itemTypeRepository;
  @Autowired private JpaItemRepository itemRepository;
  @Autowired private JpaLockPassportRepository lockRepository;
  @Autowired private JpaHumanUserPassportRepository userRepository;
  @Autowired private JpaRentRepository rentRepository;
  @Autowired
  private JpaRoleRepository roleRepository;

  private ItemType itemType;
  private Item item;
  private UUID nonExistingItemUUID;
  private HumanUserPassport user;

  @BeforeEach
  void setUp() {
    itemType = new ItemType(BigDecimal.ZERO, "name", 100, false);
    itemTypeRepository.save(itemType);
    item = new Item("name", itemType);
    itemRepository.save(item);

    user = new HumanUserPassport(123L, "FirstName", "LastName", "email@phystech.edu");
    userRepository.save(user);

    do {
    nonExistingItemUUID = UUID.randomUUID();
    } while (nonExistingItemUUID == item.getId());
  }

  @AfterEach
  void tearDown() {
    lockRepository.deleteAll();
    itemRepository.deleteAll();
    itemTypeRepository.deleteAll();
    userRepository.deleteAll();
    rentRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void testCreateLock() {
    CreateLockRequest request = new CreateLockRequest(item.getId());
    LockPassport createdLock = lockService.createLock(request);
    LockPassport retrievedLock = lockRepository.findById(createdLock.getId()).orElseThrow();

    assertNotNull(createdLock);
    assertNotNull(retrievedLock);
    assertEquals(item.getId(), createdLock.getItem().getId());
    assertEquals(item.getId(), retrievedLock.getItem().getId());
    assertFalse(createdLock.isOpen());
    assertFalse(retrievedLock.isOpen());
  }

  @Test
  void testFailCreateLockOfNotExistingItem() {
    CreateLockRequest request = new CreateLockRequest(nonExistingItemUUID);
    assertThrows(EntityNotFoundException.class, () -> lockService.createLock(request));
  }

  @Test
  void testDeleteLock() {
    CreateLockRequest request = new CreateLockRequest(item.getId());
    LockPassport lock = lockService.createLock(request);

    lockService.deleteLock(lock.getId());
    assertFalse(lockRepository.existsById(lock.getId()));
  }

  @Test
  void testUpdateItemUnderLock() throws ItemAlreadyHasLockException {
    Item newItem = new Item("other item", itemType);
    itemRepository.save(newItem);

    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    lockService.updateItemUnderLock(lock.getId(), newItem.getId());

    LockPassport updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    assertEquals(newItem.getId(), updatedLock.getItem().getId());
  }

  @Test
  void testUpdateItemUnderLockThrowsExceptionWhenItemAlreadyHasLock() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    Item newItem = new Item("other item", itemType);
    itemRepository.save(newItem);

    LockPassport anotherLock = new LockPassport(newItem, false);
    lockRepository.save(anotherLock);

    assertThrows(ItemAlreadyHasLockException.class, () -> lockService.updateItemUnderLock(lock.getId(), newItem.getId()));
  }

  @Test
  void testCanOpenLockForAdmin() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    Role adminRole = new Role("ADMIN");
    roleRepository.save(adminRole);
    user.getRoles().add(roleRepository.findById(adminRole.getId()).orElseThrow());
    userRepository.save(user);

    boolean canOpen = lockService.canUserOpenLock(user.getId(), lock.getId());
    assertTrue(canOpen);
  }

  @Test
  void testCanOpenLockForOwnRenter() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    Rent rent = new Rent(Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS), user, item);
    rentRepository.save(rent);

    boolean canOpen = lockService.canUserOpenLock(user.getId(), lock.getId());
    assertTrue(canOpen);
  }

  @Test
  void testCanOpenLockReturnFalseForWrongTime() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    Rent rent1 = new Rent(Instant.now().minus(2, ChronoUnit.HOURS), Instant.now().minus(1, ChronoUnit.HOURS), user, item);
    Rent rent2 = new Rent(Instant.now().plus(2, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS), user, item);
    rentRepository.save(rent1);
    rentRepository.save(rent2);

    assertFalse(lockService.canUserOpenLock(user.getId(), lock.getId()));
  }

  @Test
  void testCanOpenLockReturnFalseForWrongUser() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));

    HumanUserPassport otherUser = new HumanUserPassport(456L, "name", "lastName", "otheremail@phystech.edu");
    userRepository.save(otherUser);
    Rent otherUsersRent = new Rent(Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.HOURS), otherUser, item);
    rentRepository.save(otherUsersRent);

    // Return false, because the rent (even though it is going now) is owned by another user
    assertFalse(lockService.canUserOpenLock(user.getId(), lock.getId()));
  }


  @Test
  void testOpenCloseLock() {
    LockPassport lock = lockService.createLock(new CreateLockRequest(item.getId()));
    assertFalse(lock.isOpen());

    lockService.openLock(lock.getId());
    LockPassport updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    assertTrue(updatedLock.isOpen());

    lockService.closeLock(lock.getId());
    updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    assertFalse(updatedLock.isOpen());
  }
}