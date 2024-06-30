package com.mipt.hsse.hssetechbackend.lock.services;

import static org.junit.jupiter.api.Assertions.*;

import com.mipt.hsse.hssetechbackend.DatabaseSuite;
import com.mipt.hsse.hssetechbackend.data.entities.*;
import com.mipt.hsse.hssetechbackend.data.repositories.*;
import com.mipt.hsse.hssetechbackend.lock.exceptions.ItemToLockCouplingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
  @Autowired private JpaRoleRepository roleRepository;

  private Item item;
  private HumanUserPassport user;

  @BeforeEach
  void setUp() {
    ItemType itemType = new ItemType(BigDecimal.ZERO, "name", 100, false);
    itemTypeRepository.save(itemType);
    item = new Item("name", itemType);
    itemRepository.save(item);

    user = new HumanUserPassport(123L, "FirstName", "LastName", "email@phystech.edu");
    userRepository.save(user);
  }

  @AfterEach
  void tearDown() {
    itemRepository.deleteAll();
    lockRepository.deleteAll();
    itemTypeRepository.deleteAll();
    userRepository.deleteAll();
    rentRepository.deleteAll();
    roleRepository.deleteAll();
  }

  @Test
  void testCreateLock() {
    LockPassport createdLock = lockService.createLock();
    LockPassport retrievedLock = lockRepository.findById(createdLock.getId()).orElseThrow();

    assertNotNull(createdLock);
    assertNotNull(retrievedLock);
    assertFalse(createdLock.isOpen());
    assertFalse(retrievedLock.isOpen());
  }

  @Test
  void testDeleteLock() {
    LockPassport lock = lockService.createLock();
    lockService.deleteLock(lock.getId());

    assertFalse(lockRepository.existsById(lock.getId()));
  }

  @Test
  void testAddItemToLock() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    LockPassport updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();

    assertEquals(lock.getId(), updatedItem.getLock().getId());
    assertTrue(updatedLock.doesLockItem(updatedItem));
  }

  @Test
  void testAddItemUnderLockThrowsExceptionWhenItemAlreadyHasLock()
      throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    LockPassport anotherLock = new LockPassport();
    lockRepository.save(anotherLock);

    assertThrows(
        ItemToLockCouplingException.class,
        () -> lockService.addItemToLock(anotherLock.getId(), item.getId()));
  }

  @Test
  void testRemoveItemFromLock() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());
    lockService.removeItemFromLock(lock.getId(), item.getId());

    LockPassport updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    Item updatedItem = itemRepository.findById(item.getId()).orElseThrow();

    assertNull(updatedItem.getLock());
    assertFalse(updatedLock.doesLockItem(updatedItem));
  }

  @Test
  void testRemoveItemFromLockThrowsExceptionWhenItemNotLockedByThisLock() {
    LockPassport lock = lockService.createLock();
    assertThrows(
        ItemToLockCouplingException.class,
        () -> lockService.removeItemFromLock(lock.getId(), item.getId()));
  }

  @Test
  void testCanOpenLockForAdmin() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    Role adminRole = new Role("ADMIN");
    roleRepository.save(adminRole);
    user.getRoles().add(roleRepository.findById(adminRole.getId()).orElseThrow());
    userRepository.save(user);

    boolean canOpen = lockService.canUserOpenLock(user.getId(), lock.getId());
    assertTrue(canOpen);
  }

  @Test
  void testCanOpenLockForOwnRenter() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    Rent rent =
        new Rent(
            Instant.now().minus(1, ChronoUnit.HOURS),
            Instant.now().plus(1, ChronoUnit.HOURS),
            user,
            item);
    rentRepository.save(rent);

    boolean canOpen = lockService.canUserOpenLock(user.getId(), lock.getId());
    assertTrue(canOpen);
  }

  @Test
  void testCanOpenLockReturnFalseForWrongTime() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    Rent rent1 =
        new Rent(
            Instant.now().minus(2, ChronoUnit.HOURS),
            Instant.now().minus(1, ChronoUnit.HOURS),
            user,
            item);
    Rent rent2 =
        new Rent(
            Instant.now().plus(2, ChronoUnit.HOURS),
            Instant.now().plus(1, ChronoUnit.HOURS),
            user,
            item);
    rentRepository.save(rent1);
    rentRepository.save(rent2);

    assertFalse(lockService.canUserOpenLock(user.getId(), lock.getId()));
  }

  @Test
  void testCanOpenLockReturnFalseForWrongUser() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    HumanUserPassport otherUser =
        new HumanUserPassport(456L, "name", "lastName", "otheremail@phystech.edu");
    userRepository.save(otherUser);
    Rent otherUsersRent =
        new Rent(
            Instant.now().minus(1, ChronoUnit.HOURS),
            Instant.now().plus(1, ChronoUnit.HOURS),
            otherUser,
            item);
    rentRepository.save(otherUsersRent);

    // Return false, because the rent (even though it is going now) is owned by another user
    assertFalse(lockService.canUserOpenLock(user.getId(), lock.getId()));
  }

  @Test
  void testOpenCloseLock() throws ItemToLockCouplingException {
    LockPassport lock = lockService.createLock();
    lockService.addItemToLock(lock.getId(), item.getId());

    assertFalse(lock.isOpen());

    lockService.openLock(lock.getId());
    LockPassport updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    assertTrue(updatedLock.isOpen());

    lockService.closeLock(lock.getId());
    updatedLock = lockRepository.findById(lock.getId()).orElseThrow();
    assertFalse(updatedLock.isOpen());
  }
}
